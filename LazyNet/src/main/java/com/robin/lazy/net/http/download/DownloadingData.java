package com.robin.lazy.net.http.download;

/**
 * 下载中数据
 * 
 * @author 江钰锋
 * @version [版本号, 2014年10月13日]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public class DownloadingData
{
    /**
     * 下载id
     */
    private int messageId;
    
    /**
     * 下载连接
     */
    
    private String url;
    
    /**
     * 保存位置
     */
    private String savePath;
    
    /**
     * 保存的文件名
     */
    private String fileName;
    
    /**
     * 下载状态(等待状态,下载中状态,暂停状态),只有在这三种状态中的时候才会
     */
    private DownloadState downloadState;
    
    /**
     * 文件总的字节数
     */
    private long totalByte;
    
    /**
     * 当前已经下载的字节
     */
    private long currByte;
    
    private DownloadingData(Builder builder)
    {
        this.messageId = builder.messageId;
        this.url = builder.url;
        this.downloadState = builder.downloadState;
        this.savePath = builder.savePath;
        this.fileName = builder.fileName;
        this.totalByte = builder.totalByte;
        this.currByte = builder.currByte;
    }
    
    /**
     * 
     * 构建器
     * 
     * @author 江钰锋
     * @version [版本号, 2014年12月25日]
     * @see [相关类/方法]
     * @since [产品/模块版本]
     */
    public static class Builder
    {
        private int messageId;
        
        private String url;
        
        private DownloadState downloadState;
        
        private String savePath;
        
        private String fileName;
        
        private long totalByte;
        
        private long currByte;
        
        public Builder(int messageId)
        {
            this.messageId = messageId;
        }
        
        public Builder setMessageId(int messageId)
        {
            this.messageId = messageId;
            return this;
        }
        
        public Builder setUrl(String bUrl)
        {
            url = bUrl;
            return this;
        }
        
        public Builder setDownloadState(DownloadState bDownloadState)
        {
            this.downloadState = bDownloadState;
            return this;
        }
        
        public Builder setSavePath(String bSavePath)
        {
            this.savePath = bSavePath;
            return this;
        }
        
        public Builder setFileName(String bFileName)
        {
            this.fileName = bFileName;
            return this;
        }
        
        public Builder setTotalByte(long bTotalByte)
        {
            this.totalByte = bTotalByte;
            return this;
        }
        
        public Builder setCurrByte(long bCurrByte)
        {
            this.currByte = bCurrByte;
            return this;
        }
        
        /**
         * 构造器入口
         * 
         * @return
         * @see [类、类#方法、类#成员]
         */
        public DownloadingData build()
        {
            return new DownloadingData(this);
        }
    }
    
    public int getMessageId()
    {
        return messageId;
    }
    
    public void setMessageId(int messageId)
    {
        this.messageId = messageId;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getSavePath()
    {
        return savePath;
    }
    
    public void setSavePath(String savePath)
    {
        this.savePath = savePath;
    }
    
    public String getFileName()
    {
        return fileName;
    }
    
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    
    public DownloadState getDownloadState()
    {
        return downloadState;
    }
    
    public void setDownloadState(DownloadState downloadState)
    {
        this.downloadState = downloadState;
    }
    
    public long getTotalByte()
    {
        return totalByte;
    }
    
    public void setTotalByte(long totalByte)
    {
        this.totalByte = totalByte;
    }
    
    public long getCurrByte()
    {
        return currByte;
    }
    
    public void setCurrByte(long currByte)
    {
        this.currByte = currByte;
    }
    
}
