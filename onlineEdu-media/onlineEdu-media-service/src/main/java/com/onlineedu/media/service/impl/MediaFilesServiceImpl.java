package com.onlineedu.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.onlineedu.base.exception.BusinessException;
import com.onlineedu.base.exception.CommonError;
import com.onlineedu.base.model.PageParams;
import com.onlineedu.base.model.Result;
import com.onlineedu.media.mapper.MediaFilesMapper;
import com.onlineedu.media.model.dto.QueryMediaParamsDto;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.model.dto.UploadFileResultDto;
import com.onlineedu.media.model.entities.MediaFiles;
import com.onlineedu.media.service.MediaFilesService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.UploadObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static com.onlineedu.base.model.SystemCode.CODE_UNKOWN_ERROR;
import static com.onlineedu.base.model.SystemStatus.PUBLIC_STATUS_USING;

/**
* @author cheems
* @description 针对表【media_files(媒资信息)】的数据库操作Service实现
* @createDate 2023-02-01 23:18:26
*/
@Service
@Slf4j
public class MediaFilesServiceImpl extends ServiceImpl<MediaFilesMapper, MediaFiles>
    implements MediaFilesService{

    @Resource
    private MinioClient minioClient;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @Lazy
    @Resource
    private MediaFilesService mediaServiceProxy;

    //普通文件bucket
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    @Override
    public Result getMediaFileList(PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename())
                .eq(StrUtil.isNotBlank(queryMediaParamsDto.getFileType()),MediaFiles::getFileType,queryMediaParamsDto.getFileType())
                .eq(StrUtil.isNotBlank(queryMediaParamsDto.getAuditStatus()),MediaFiles::getFileType,queryMediaParamsDto.getAuditStatus());

        Page<MediaFiles> mediaFilesPage = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<MediaFiles> page = page(mediaFilesPage,wrapper);
        return Result.success(page);
    }

    /**
     * 上传文件(图片&&其他)
     * @param companyId 上传的机构Id
     * @param uploadFileParamsDto 文件上传参数
     * @param bytes 文件本身的字节数组
     * @param folder 文件存储路径(不要求必传)
     * @param objectName 文件存储名称(不要求必传)
     * @return
     */
    @Override
    public UploadFileResultDto upload(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) throws Exception {
        //获取文件Md5值
        String md5 = MD5.create().digestHex(bytes);
        String filename = uploadFileParamsDto.getFilename();
        //构造objectName
        if(StrUtil.isBlank(objectName)){
            String extensionName = filename.substring(filename.lastIndexOf(".")); //文件后缀名
            objectName = md5 + extensionName;
        }
        //构造folder
        if(StrUtil.isBlank(folder)){
            folder =  generateFolderByDate(new Date(),true,true,true);
        }
        if(!folder.contains("/")){
            folder = folder + "/";
        }
        //构造object (路径 + 文件名)
        objectName = folder + objectName;

        //1. 上传文件到minio
        uploadFileToMinio(bytes,bucketFiles,objectName);

        //2. 保存文件信息到数据库 (使用mediaFilesService代理对象调用 不然事务会失效)
        MediaFiles mediaFiles = mediaServiceProxy.saveFileMessageToDb(companyId, md5, uploadFileParamsDto, bucketFiles, objectName);

        //3. 构造返回数据
        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtil.copyProperties(mediaFiles, uploadFileResultDto);
        return uploadFileResultDto;
    }

    /**
     * 上传文件至 minio
     * @param bytes 文件字节数组
     * @param bucketName 文件要存储的bucket
     * @param objectName object名称(路径+名称)
     */
    public void uploadFileToMinio(byte[] bytes, String bucketName, String objectName) throws Exception {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            String extension = objectName.substring(objectName.lastIndexOf("."));
            String  contentType = getMimeTypeByExtension(extension);

            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(is, is.available(), -1) //-1 指定分块大小按5m分(最小5m 最大5t 最多1w片)
                    .contentType(contentType)
                    .build();

            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            throw new BusinessException(CODE_UNKOWN_ERROR,"文件上传失败");
        }
    }

    /**
     * 保存文件信息到数据库
     * @param companyId 机构ID
     * @param fileMd5 文件MD5值
     * @param uploadFileParamsDto 上传文件携带的参数
     * @param bucket bucket名称
     * @param objectName object名称(路径+名称)
     * @return
     * @throws BusinessException
     */
    @Transactional
    public MediaFiles saveFileMessageToDb(Long companyId,String fileMd5,UploadFileParamsDto uploadFileParamsDto,String bucket,String objectName) throws BusinessException {
        MediaFiles files = mediaFilesMapper.selectById(fileMd5);
        MediaFiles mediaFiles = new MediaFiles();
        if(files == null){
            BeanUtil.copyProperties(uploadFileParamsDto,mediaFiles);
            mediaFiles.setId(fileMd5);      //文件的md5作为主键
            mediaFiles.setFileId(fileMd5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/" + bucket + "/" + objectName); //文件的访问路径 bucket名 + 文件路径 + 文件对象名
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(new Date());
            mediaFiles.setStatus(PUBLIC_STATUS_USING);
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert <= 0){
                throw new BusinessException(CommonError.INSERT_EXCEPTION);
            }
        }
        return mediaFiles;
    }

    //根据文件扩展名获取Mine类型
    private String getMimeTypeByExtension(String extension){
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if(StrUtil.isNotEmpty(extension)){
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if(extensionMatch!=null){
                contentType = extensionMatch.getMimeType();
            }
        }
        return contentType;

    }


    //通过时间生成对应的目录
    private String generateFolderByDate (Date date, boolean year, boolean mouth, boolean day) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String formatStr = sdf.format(date);
        String[] dateArr = formatStr.split("-");
        StringBuffer stringBuffer = new StringBuffer();
        if(year){
            stringBuffer.append(dateArr[0]).append("/");
        }
        if(mouth){
            stringBuffer.append(dateArr[1]).append("/");;
        }
        if(day){
            stringBuffer.append(dateArr[2]).append("/");;
        }
        return stringBuffer.toString();
    }
}




