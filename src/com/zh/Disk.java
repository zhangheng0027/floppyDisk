package com.zh;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

/**
 * Created by zh on 2020/4/7.
 */
public class Disk {

    public static final int SECTOR_SIZE = 512; // 一个扇区 512 字节
    public static final int SECTOR_COUNT = 18; // 一个磁道 18 个扇区
    public static final int CYLINDER_SIZE = 80;  // 一个柱面 80 个磁道
    public static final int CYLINDER_COUNT = 2; // 2 个柱面

    private int index_sector = 1;  // 当前指向的扇区 起始 1
    private int index_magnetic = 0; // 指向的磁道 起始 0
    private int index_cylinder = 0; // 指向的柱面 起始 0

    private ArrayList<ArrayList<ArrayList<byte[]>>> disk = new ArrayList<>();

    public Disk(String fileName) {
        init();
        reloadDiskFile(fileName);
    }

    public Disk() {
        init();
    }

    /**
     * 将虚拟软盘文件加载到内存中
     * @param filePathName 文件路径及名称
     */
    public void reloadDiskFile(String filePathName) {
        try {
            File file = new File(filePathName);
            byte[] bytes = Files.readAllBytes(file.toPath());
            int count = bytes.length / SECTOR_SIZE;
            clear();
            initIndexSector(1);
            initIndexMagnetic(0);
            initIndexCylinder(0);
            for (int i = 0; i < count; i++) {
                System.arraycopy(bytes, i * SECTOR_SIZE,
                        disk.get(index_cylinder).get(index_magnetic).get(index_sector - 1), 0, SECTOR_SIZE);
                nextIndex();
            }
            System.arraycopy(bytes, count * SECTOR_SIZE,
                    disk.get(index_cylinder).get(index_magnetic).get(index_sector - 1), 0, bytes.length - count * SECTOR_SIZE);
        } catch (IOException e) {}
    }

    /**
     * 读取软盘
     * @param sector
     * @param magnetic
     * @param cylinder
     * @param count  连续读取 count 个扇区
     * @return
     */
    public byte[] readDisk(int sector, int magnetic, int cylinder, int count) {
        if (count < 1)
            count = 1;
        byte[] b = new byte[count * SECTOR_SIZE];
        initIndexCylinder(cylinder);
        initIndexMagnetic(magnetic);
        initIndexSector(sector);
        for (int i = 0; i < count; i++) {
            System.arraycopy(disk.get(index_cylinder).get(index_magnetic).get(index_sector - 1), 0,
                    b, i * SECTOR_SIZE, SECTOR_SIZE);
            nextIndex();
        }
        return b;
    }

    /**
     * 将内容写入虚拟软盘
     * @param cont
     * @param sector
     * @param magnetic
     * @param cylinder
     */
    public void writeDisk(byte[] cont, int sector, int magnetic, int cylinder) {
        int count = cont.length / SECTOR_SIZE;
        initIndexCylinder(cylinder);
        initIndexMagnetic(magnetic);
        initIndexSector(sector);
        for (int i = 0; i < count; i++) {
            System.arraycopy(cont, i * SECTOR_SIZE,
                    disk.get(index_cylinder).get(index_magnetic).get(index_sector - 1), 0, SECTOR_SIZE);
            nextIndex();
        }
        System.arraycopy(cont, count * SECTOR_SIZE,
                disk.get(index_cylinder).get(index_magnetic).get(index_sector - 1), 0, cont.length - count * SECTOR_SIZE);
    }

    /**
     * 保存
     * @return
     */
    public boolean saveDisk(String filePathName) {
        boolean flag = false;
        createFile(filePathName);
        try (FileOutputStream fos = new FileOutputStream(filePathName);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.write(readDisk(1, 0, 0, SECTOR_COUNT * CYLINDER_SIZE * CYLINDER_COUNT));
            flag = true;
        } catch (IOException e) {
            throw new Error();
        }
        return flag;
    }

    /**
     * 创建文件，如果文件存在，先删除，在创建
     */
    private void createFile(String filePathName) {
        if (null == filePathName)
            throw new Error();
        File f = new File(filePathName);
        try {
            if (f.exists())
                f.delete();
            f.createNewFile();
        } catch (IOException e) {
            throw new Error();
        }
    }

    /**
     * 初始化柱面指向
     * @param cylinder
     */
    public void  initIndexCylinder(int cylinder) {
        if (cylinder >= CYLINDER_COUNT)
            index_cylinder = CYLINDER_COUNT - 1;
        if (cylinder < 0)
            index_cylinder = 0;
        index_cylinder = cylinder;
    }

    /**
     * 初始化磁道指向
     * @param magnetic
     */
    public void initIndexMagnetic(int magnetic) {
        if (magnetic >= CYLINDER_SIZE)
            index_magnetic = CYLINDER_SIZE - 1;
        if (magnetic < 0)
            index_magnetic = 0;
        index_magnetic = magnetic;
    }

    /**
     * 初始化扇区指向
     * @param sector
     */
    public void initIndexSector(int sector) {
        if (sector > SECTOR_COUNT)
            index_sector = SECTOR_COUNT;
        if (sector < 1)
            index_sector = 1;
        index_sector = sector;
    }

    /**
     * 指向下一个位置
     * 循环
     */
    private void nextIndex() {
        index_sector++;
        if (index_sector <= SECTOR_COUNT)
            return;
        index_sector = 1;
        index_cylinder++;
        if (index_cylinder < CYLINDER_COUNT)
            return;
        index_cylinder = 0;
        index_magnetic++;
        if (index_magnetic >= CYLINDER_SIZE)
            index_magnetic = 0;
    }

    /**
     * 初始化整个磁盘
     */
    public void init() {
        for (int i = 0; i < CYLINDER_COUNT; i++) {
            disk.add(initCylinder());
        }
    }

    /**
     * 初始化单个柱面
     */
    private ArrayList<ArrayList<byte[]>> initCylinder() {
        ArrayList<ArrayList<byte[]>> cylinder = new ArrayList<>();
        for (int i = 0; i < CYLINDER_SIZE; i++) {
            cylinder.add(initSector());
        }
        return cylinder;
    }

    /**
     * 初始化单个磁道
     */
    private ArrayList<byte[]> initSector() {
        ArrayList<byte[]> sector = new ArrayList<>();
        for (int i = 0; i < SECTOR_COUNT; i++)
            sector.add(new byte[SECTOR_SIZE]);
        return sector;
    }

    private void clear() {
        disk.clear();
        init();
    }

}
