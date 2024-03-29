<template>
  <a-layout>
    <a-layout-content :style="{ background: 'linear-gradient(87deg, #a1ffce, #faffd1)', padding: '24px', margin: 0, minHeight: '280px' }">
      <h3 v-if="level1.length === 0">对不起，找不到相关文档！</h3>
      <a-row>
        <a-col :span="6">
          <a-tree :style="{}"
            v-if="level1.length > 0"
            :tree-data="level1"
            @select="onSelect"
            :replaceFields="{title: 'name', key: 'id', value: 'id'}"
            :defaultExpandAll="true"
            :defaultSelectedKeys="defaultSelectedKeys"
          >
          </a-tree>
        </a-col>
        <a-col :span="18">
          <div>
            <h2 style="text-align: center; font-size: 26px; font-weight: 500;">{{doc.name}}</h2>
            <div style="text-align: center">
              <span style="font-size: 16px"><component v-bind:is="'UserOutlined'" style="margin-right: 8px" />Amber-L</span>&nbsp; &nbsp;
              <span style="font-size: 16px"><component v-bind:is="'ClockCircleOutlined'" style="margin-right: 8px" />{{doc.uptTime}}</span>&nbsp; &nbsp;
              <span style="font-size: 16px"><component v-bind:is="'EyeOutlined'" style="margin-right: 8px" />{{doc.viewCount}}</span> &nbsp; &nbsp;
              <span style="font-size: 16px"><component v-bind:is="'LikeOutlined'" style="margin-right: 8px" />{{doc.voteCount}}</span>&nbsp; &nbsp;
            </div>
            <a-divider style="height: 2px; background-color: #9999cc"/>
          </div>

          <div class="vditor-reset" :innerHTML="html"></div>
          <div class="vote-div">
            <a-button type="primary" shape="round" :size="'large'" @click="vote">
              <template #icon><LikeOutlined /> &nbsp;点赞：{{doc.voteCount}} </template>
            </a-button>
          </div>
        </a-col>
      </a-row>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts">
  import { defineComponent, onMounted, ref, createVNode } from 'vue';
  import axios from 'axios';
  import {message} from 'ant-design-vue';
  import {Tool} from "@/util/tool";
  import {useRoute} from "vue-router";
  import Vditor from 'vditor'
  import "vditor/dist/index.css"
  export default defineComponent({
    name: 'Doc',
    setup() {
      const route = useRoute();
      const docs = ref();
      const html = ref();
      const defaultSelectedKeys = ref();
      defaultSelectedKeys.value = [];
      // 当前选中的文档
      const doc = ref();
      doc.value = {};

      /**
       * 一级文档树，children属性就是二级文档
       * [{
       *   id: "",
       *   name: "",
       *   children: [{
       *     id: "",
       *     name: "",
       *   }]
       * }]
       */
      const level1 = ref(); // 一级文档树，children属性就是二级文档
      level1.value = [];

      /**
       * 内容查询
       **/
      const handleQueryContent = (id: number) => {
        axios.get("/doc/findContent/" + id).then((response) => {
          const data = response.data;
          if (data.success) {
            html.value = data.data;
          } else {
            message.error(data.msg);
          }
        });
      };

      /**
       * 数据查询
       **/
      const handleQuery = () => {
        axios.get("/doc/getDocListByEbookId/" + route.query.ebookId).then((response) => {
          const data = response.data;
          if (data.success) {
            docs.value = data.data;

            level1.value = [];
            level1.value = Tool.array2Tree(docs.value, 0);

            if (Tool.isNotEmpty(level1)) {
              defaultSelectedKeys.value = [level1.value[0].id];
              handleQueryContent(level1.value[0].id);
              // 初始显示文档信息
              doc.value = level1.value[0];
            }
          } else {
            message.error(data.msg);
          }
        });
      };

      const onSelect = (selectedKeys: any, info: any) => {
        console.log('selected', selectedKeys, info);
        if (Tool.isNotEmpty(selectedKeys)) {
          // 选中某一节点时，加载该节点的文档信息
          doc.value = info.selectedNodes[0].props;
          // 加载内容
          handleQueryContent(selectedKeys[0]);
        }
      };

      // 点赞
      const vote = () => {
        axios.get('/doc/vote/' + doc.value.id).then((response) => {
          const data = response.data;
          if (data.success) {
            doc.value.voteCount++;
          } else {
            message.error(data.msg);
          }
        });
      };

      onMounted(() => {
        handleQuery();
      });

      return {
        level1,
        html,
        onSelect,
        defaultSelectedKeys,
        doc,
        vote,
      }
    }
  });
</script>

<style>
  /* wangeditor默认样式, 参照: http://www.wangeditor.com/doc/pages/02-%E5%86%85%E5%AE%B9%E5%A4%84%E7%90%86/03-%E8%8E%B7%E5%8F%96html.html */
  /* table 样式 */
  .vditor-reset table {
    border-top: 1px solid #ccc;
    border-left: 1px solid #ccc;
    border-collapse: collapse;
  }
  .vditor-reset table td,
  .vditor-reset table th {
    border-bottom: 1px solid #ccc;
    border-right: 1px solid #ccc;
    padding: 3px 5px;
    text-align: left;
  }
  .vditor-reset table th {
    border-bottom: 2px solid #ccc;
    text-align: center;
  }

  /* blockquote 样式 */
  .vditor-reset blockquote {
    display: block;
    border-left: 8px solid #d0e5f2;
    padding: 5px 10px;
    margin: 10px 0;
    line-height: 1.4;
    font-size: 100%;
    background-color: #f1f1f1;
    white-space: pre-wrap; /* 显示空格 */
  }

  /* code 样式 */
  .vditor-reset code {
    display: inline-block;
    *display: inline;
    *zoom: 1;
    background-color: #fafafa;
    border-radius: 5px;
    border: 1px solid hsl(0, 0%, 91%);
    margin: 0 3px;
	font-family: "lucida console";
    padding: 10px;
    font-size: 14px!important;
    max-width: 100%;
    height: auto;
  }
  .vditor-reset pre code {
    display: block;
    background: linear-gradient(60deg, #50c9c3, #96deda) !important;
    padding: 10px;
  }

  /* ul ol 样式 */
  .wangeditor ul, ol {
    margin: 10px 0 10px 20px;
  }

  /* 和antdv p冲突，覆盖掉 */
  .vditor-reset blockquote p {
    font-family:"Microsoft YaHei";
    margin: 20px 10px !important;
    font-size: 14px !important;
    font-weight:500;
  }

  /* 点赞 */
  .vote-div {
    padding: 15px;
    text-align: center;
  }

  /* 图片自适应 */
  .wangeditor img {
    max-width: 100%;
    height: auto;
  }

  /* 视频自适应 */
  .wangeditor iframe {
    width: 100%;
    height: 400px;
  }


</style>
