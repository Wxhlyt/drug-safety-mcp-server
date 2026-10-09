<script setup>
import SourceEvidencePanel from '../../components/SourceEvidencePanel.vue'
</script>

<template>
  <div class="page">
    <h2>不良反应管理</h2>
    <SourceEvidencePanel kind="adverse" />
  </div>
</template>

<style scoped>
.page { background: #fff; padding: 20px; border-radius: 8px; }
h2 { margin: 0 0 20px; color: #303133; }
</style>
