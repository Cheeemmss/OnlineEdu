package com.onlineedu.media.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
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
import com.onlineedu.base.model.SystemCode;
import com.onlineedu.media.mapper.MediaFilesMapper;
import com.onlineedu.media.mapper.MediaProcessMapper;
import com.onlineedu.media.model.dto.QueryMediaParamsDto;
import com.onlineedu.media.model.dto.UploadFileParamsDto;
import com.onlineedu.media.model.dto.UploadFileResultDto;
import com.onlineedu.media.model.entities.MediaFiles;
import com.onlineedu.media.model.entities.MediaProcess;
import com.onlineedu.media.service.MediaFilesService;
import io.minio.*;
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
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import static com.onlineedu.base.model.SystemCode.CODE_UNKOWN_ERROR;
import static com.onlineedu.base.model.SystemStatus.*;

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

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Lazy
    @Resource
    private MediaFilesService mediaServiceProxy;

    //普通文件bucket
    @Value("${minio.bucket.files}")
    private String bucketFiles;

    //视频文件bucket
    @Value("${minio.bucket.videofiles}")
    private String videoFiles;

    @Override
    public Result getMediaFileList(PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(queryMediaParamsDto.getFilename()),MediaFiles::getFilename,queryMediaParamsDto.getFilename())
                .eq(StrUtil.isNotBlank(queryMediaParamsDto.getFileType()),MediaFiles::getFileType,queryMediaParamsDto.getFileType())
                .eq(StrUtil.isNotBlank(queryMediaParamsDto.getAuditStatus()),MediaFiles::getAuditStatus,queryMediaParamsDto.getAuditStatus())
                 .orderByDesc(MediaFiles::getCreateDate);

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
        String md5 = DigestUtil.md5Hex(bytes);
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
            e.printStackTrace();
            throw new BusinessException(CODE_UNKOWN_ERROR,"文件上传失败");
        }
    }

    /**
     * 上传文件至 minio
     * @param filePath 要上传的文件的本地路径
     * @param bucketName 文件要存储的bucket
     * @param objectName object名称(路径+名称)
     */
    public void uploadFileToMinio(String filePath, String bucketName, String objectName) throws Exception {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .build();

            minioClient.uploadObject(uploadObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CODE_UNKOWN_ERROR,"文件上传失败");
        }
    }

    @Override
    public Result getAuditedMediasList(Long companyId,String mediaName) {
        LambdaQueryWrapper<MediaFiles> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MediaFiles::getCompanyId,companyId)
                .eq(MediaFiles::getAuditStatus,OBJECT_AUDIT_SUCCESS)
                .eq(MediaFiles::getFileType,FILE_TYPE_VIDEO)
                .like(MediaFiles::getFilename,mediaName)
                .orderByDesc(MediaFiles::getCreateDate);

        return Result.success("",mediaFilesMapper.selectList(wrapper));
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

            String filename = mediaFiles.getFilename();
            String extension = filename.substring(filename.lastIndexOf("."));
            String fileType = getMimeTypeByExtension(extension);
            //只有图片类型和mp4类型才可以直接设置访问url(如avi格式需要转码后才可设置)
            if(fileType.contains("image") || fileType.contains("mp4")){
                mediaFiles.setUrl("/" + bucket + "/" + objectName); //文件的访问路径 bucket名 + 文件路径 + 文件对象名
            }
            mediaFiles.setBucket(bucket);
            mediaFiles.setCreateDate(new Date());
            mediaFiles.setStatus(PUBLIC_STATUS_USING);
            mediaFiles.setAuditStatus(OBJECT_AUDIT_NOT_AUDIT);
            int insert = mediaFilesMapper.insert(mediaFiles);
            if(insert <= 0){
                throw new BusinessException(CommonError.INSERT_EXCEPTION);
            }

            //若是avi格式的视频需要添加 到待处理表中
            if(fileType.contains("video/x-msvideo")){
                MediaProcess mediaProcess = new MediaProcess();
                BeanUtil.copyProperties(mediaFiles,mediaProcess,"id");
                mediaProcess.setStatus(MEDIA_PROCESS_UN_PROCESS); //设为未处理
                int ins = mediaProcessMapper.insert(mediaProcess);
                if(ins <= 0){
                    throw new BusinessException(CommonError.INSERT_EXCEPTION);
                }
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

    /**
     * 通过文件MD5检查是否上传过该文件
     * @param fileMd5
     * @return
     */
    @Override
    public Result checkFileIsExistByMd5(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if(mediaFiles == null){
            return Result.success(false);
        }
        return Result.success(true);
    }

    /**
     * 检查分片文件是否存在
     * @param fileMd5 文件md5
     * @param chunkIndex 分片文件索引
     * @return
     * @throws BusinessException
     */
    @Override
    public Result checkChunkFileIsExistByMd5(String fileMd5,Integer chunkIndex) throws BusinessException {
        String chunkFileObject = generateFolderByFileMd5(fileMd5) + chunkIndex;
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(videoFiles)
                .object(chunkFileObject)
                .build();

        //这里获取的是文件输入流
        try {
            InputStream is = minioClient.getObject(getObjectArgs);
            if(is != null){
                return Result.success(true);
            }
        } catch (Exception e) {
            log.info("分片不存在,chunkIndex{}",chunkIndex);
        }
        return Result.success(false);
    }

    /**
     * 上传分片文件
     * @param bytes 分片文件的字节数组
     * @param chunkIndex 分片文件索引
     * @param fileMd5  文件的 md5
     * @return
     */
    @Override
    public Result uploadChunkFile(byte[] bytes, Integer chunkIndex, String fileMd5) throws BusinessException {
        String chunkObject = generateFolderByFileMd5(fileMd5) + chunkIndex;
        log.info(chunkObject);
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()   //这存的文件都没有后缀名 没法服用前面的uploadToMinio方法
                    .bucket(videoFiles)
                    .object(chunkObject)
                    .stream(is,is.available(), -1)
                    .build();
            minioClient.putObject(putObjectArgs);
            return Result.success(true);
        } catch (Exception e) {
            log.info("分片文件上传异常,chunkIndex:{}",chunkIndex);
        }
        return Result.success(false);
    }


    /**
     * 合并分块
     * @param companyId 机构id
     * @param fileMd5 合并文件的Md5
     * @param chunkNum 分块总数
     * @param uploadFileParamsDto 文件上传的参数
     * @return
     * @throws BusinessException
     */
    @Override
    public Result mergeChunkFiles(Long companyId,String fileMd5, Integer chunkNum,UploadFileParamsDto uploadFileParamsDto) throws BusinessException {
        //1. 将minio中的分片文件先保存到本地
        File[] chunkFiles = getChunkFiles(fileMd5, chunkNum);
        //2. 合并分片
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));
        File mergeFile = null;
        RandomAccessFile writer = null;
        RandomAccessFile reader = null;
        try {
            mergeFile =  File.createTempFile(filename, extension);
            log.info("mergeFile temp path:{}",mergeFile.getAbsolutePath());
            writer = new RandomAccessFile(mergeFile, "rw");
            for (File chunkFile : chunkFiles) {
                reader = new RandomAccessFile(chunkFile, "r");
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = reader.read(buffer)) != -1){
                    writer.write(buffer,0,len);
                }
                reader.close(); //这里的reader流必须开一个 用完关一个 不能在最后关 那样只会关最后一个流 导致前面创建的临时文件删除不掉
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(writer != null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //3. 检查合并后的文件和原来的文件的md5是否相同
        String mergeFileMd5 = DigestUtil.md5Hex(mergeFile);
        if(!fileMd5.equals(mergeFileMd5)){
            log.info("上传失败: 文件md5匹配不上");
            throw new BusinessException(CODE_UNKOWN_ERROR,"合并失败");
        }
        uploadFileParamsDto.setFileSize(mergeFile.length());

        //4. 上传合并后的文件至minio
        byte[] bytes = FileUtil.readBytes(mergeFile);
        String mergeObjectName = fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + extension;
        try {
            uploadFileToMinio(bytes,videoFiles,mergeObjectName);
            //5. 保存文件信息到数据库
            saveFileMessageToDb(companyId, mergeFileMd5, uploadFileParamsDto, videoFiles, mergeObjectName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CODE_UNKOWN_ERROR,"文件上传失败");
        }finally {
            //6. 删除临时文件
            for (File file : chunkFiles) {
                if(file.exists()){
                    boolean delete = file.delete();
                    log.info("删除临时文件:{} delete:{}",file.getAbsolutePath(),delete);
                }
            }
            if(mergeFile.exists()){
                mergeFile.delete();
            }
        }
        return Result.success(true);
    }

    //从minio把某个大文件的所有分片文件下载保存为本地临时文件 返回这些本地临时文件对应的File对象的数组
    private File[] getChunkFiles(String fileMd5,Integer chunkNum) throws BusinessException {
        String chunkFolder = generateFolderByFileMd5(fileMd5);
        File[] chunkFiles = new File[chunkNum];
        try {
            for (int i = 0; i < chunkNum; i++) {
                String chunkFileObject = chunkFolder + i;
                File chunkFile = File.createTempFile("chunkFile", null); //创建临时文件(前缀名为chunkFile)
                File file = downloadFileFromMinio(chunkFile, videoFiles, chunkFileObject);
                chunkFiles[i] = file; //这样后面就不用排序了 分片文件自然是有序的
            }
            return chunkFiles;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(CODE_UNKOWN_ERROR,"分片文件下载失败");
        }
    }

    /**
     * 从Minio下载文件
     * @param file 本地的文件对应的file对象
     * @param bucket Bucket名称
     * @param objectName object名
     * @return 本地的文件对应的file对象
     */
    @Override
    public File downloadFileFromMinio(File file,String bucket,String objectName) throws BusinessException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = minioClient.getObject(getObjectArgs);
            os = new FileOutputStream(file);
            IoUtil.copy(is,os);
            return file;
        } catch (Exception e) {
            log.info("minio文件下载失败,bucket:{},object:{}",bucket,objectName);
            throw new BusinessException(CODE_UNKOWN_ERROR,"minio文件下载失败");
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    //根据文件的md5生成目录(第一位作为一级目录 第二位作为二级目录 文件Md5作为三级目录 第三级目录中存放合并后的文件以及四级目录(存放分片文件的目录))
    private String generateFolderByFileMd5(String fileMd5){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunkFiles/";
    };

    @Override
    public Result getMediaUrlById(String mediaId) throws BusinessException {
        MediaFiles mediaFile = mediaFilesMapper.selectById(mediaId);
        if(mediaFile == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"该文件不存在");
        }
        if(mediaFile.getUrl() == null){
            throw new BusinessException(CODE_UNKOWN_ERROR,"该文件还未被转码处理 请稍后");
        }
        return Result.success("",mediaFile.getUrl());
    }
}




