<template>
  <AppActionLogAppender ref="logView"/>
</template>

<script>
import AppActionLogAppender from '@/components/log/AppActionLogAppender'

export default {
  name: 'AppActionLogView',
  components: {
    AppActionLogAppender
  },
  mounted() {
    this.$refs.logView.open(parseInt(this.$route.params.id))
  }
}
</script>

<style lang="less" scoped>
</style>
