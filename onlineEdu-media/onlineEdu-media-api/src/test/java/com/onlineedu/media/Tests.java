package com.onlineedu.media;

import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @Author cheems
 * @Date 2023/2/2 16:26
 */
@SpringBootTest
public class Tests {

    @Test
    public void test1() throws Exception {
        File source = new File("I:\\chunkTest\\resources\\League of Legends (TM) Client 2021-12-30 23-14-19.mp4");
        File chunkTarget = new File("I:\\chunkTest\\target");

        //分块大小
        int chunkSize = 1024 * 1024;  //1mb

        //计算分块数目(向上取整)
        double chunkNum = Math.ceil(source.length() * 1.0 / chunkSize);

        //读取源文件所有数据 分别写给一块一块的分块 每个分块写满就不写了 继续写下一个分块
        RandomAccessFile reader = new RandomAccessFile(source, "r");

        for(int i = 0;i < chunkNum; i++){
            File file = new File("I:\\chunkTest\\target\\" + i);
            boolean newFile = file.createNewFile();
            if(newFile){
                RandomAccessFile writer = new RandomAccessFile(file, "rw");
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = reader.read(buffer)) != -1){
                    writer.write(buffer,0,len);
                    if(file.length() >= chunkSize) break; //当前分块写满了 继续写下一个
                }
                writer.close();
            }
        }
        reader.close();
    }

    @Test
    public void test2() throws Exception {
        File newFile = new File("I:\\chunkTest\\newFile\\chunkTestFile_2.mp4");
        File chunkTarget = new File("I:\\chunkTest\\target\\");
        File[] files = chunkTarget.listFiles();
        List<File> fileList = Arrays.asList(files);
        //根据文件的名称将文件进行排序
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        RandomAccessFile writer = new RandomAccessFile(newFile, "rw");
        //依次读取分块文件中的内容 并写入到合并文件当中
        for (File file : fileList) {
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = reader.read(buffer)) != -1){
                writer.write(buffer,0,len);
            }
            reader.close();
        }
        writer.close();

        //通过Md5判断合并后的文件是否和源文件相同
        File source = new File("I:\\chunkTest\\resources\\VID_20210707_222844.mp4");
        String sourceMd5 = MD5.create().digestHex(source);
        String newFileMd5 = MD5.create().digestHex(newFile);
        if(sourceMd5.equals(newFileMd5)){
            System.out.println("合并成功");
        }

    }
}
