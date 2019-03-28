package com.gnos.model;

public class Song {
    //歌曲名称
    private String mSongName;

    //歌曲文件名
    private String mSongFileName;

    //歌曲名字长度
    private int mNameLength;

    //歌名转换成相应字符数组
    public char[] getNameCharacters() {
        return mSongName.toCharArray();
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String SongName) {
        this.mSongName = SongName;

        //获得歌曲名字长度
        this.mNameLength = SongName.length();
    }

    public String getSongFileName() {
        return mSongFileName;
    }

    public void setSongFileName(String SongFileName) {
        this.mSongFileName = SongFileName;
    }

    public int getNameLength() {
        return mNameLength;
    }
}
