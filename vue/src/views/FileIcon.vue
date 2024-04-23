<template>
  <span>
    <i v-if="isFolder">
      <img class="file-icon" src="@/assets/imgs/folder.svg" alt="文件夹">
    </i>
    <span v-else>
      <img v-if="iconType === 'compressed'" src="@/assets/imgs/compressed.svg" alt="压缩包" class="file-icon">
      <img v-else-if="iconType === 'video'" src="@/assets/imgs/video.svg" alt="视频文件" class="file-icon">
      <img v-else-if="iconType === 'audio'" src="@/assets/imgs/audio.svg" alt="声音文件" class="file-icon">
      <img v-else-if="iconType === 'image'" src="@/assets/imgs/image.svg" alt="图片文件" class="file-icon">
      <img v-else-if="iconType === 'text'" src="@/assets/imgs/text.svg" alt="文本文件" class="file-icon">
      <img v-else src="@/assets/imgs/unknown.svg" alt="未知文件" class="file-icon">
    </span>
  </span>
</template>

<script>
export default {
  props: {
    fileType: String,
    isFolder: Boolean
  },
  computed: {
    iconType() {
      const typeMap = {
        compressed: ["zip", "rar", "7z", "tar", "gz", "bz2"],
        video: ["mp4", "avi", "rmvb", "wmv", "flv"],
        audio: ["mp3", "wav", "wma", "aac", "flac"],
        image: ["jpg", "jpeg", "png", "gif", "bmp", "psd", "webp", "ico"],
        text: ["txt", "doc", "docx", "pdf", "md", "html", "css", "js"]
      };

      const extension = this.fileType.toLowerCase(); // 将后缀名转换为小写，以便匹配
      let iconType = "unknown"; // 默认为未知文件类型
      for (const type in typeMap) {
        if (typeMap[type].includes(extension)) {
          iconType = type;
          break;
        }
      }
      return iconType;
    }
  }
};
</script>

<style scoped>
.file-icon {
  width: 100%;
}
</style>
