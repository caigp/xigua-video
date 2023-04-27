package com.xiaocai.xiguavideo.page

import android.util.Log
import com.su.mediabox.pluginapi.action.DetailAction
import com.su.mediabox.pluginapi.components.IMediaSearchPageDataComponent
import com.su.mediabox.pluginapi.data.BaseData
import com.su.mediabox.pluginapi.data.MediaInfo1Data
import com.su.mediabox.pluginapi.data.MediaInfo2Data
import com.xiaocai.xiguavideo.http.HttpUtils
import okhttp3.Headers
import org.json.JSONObject
import java.net.URLEncoder

class MediaSearchPageDataComponent : IMediaSearchPageDataComponent {

    private val TAG = MediaSearchPageDataComponent::class.java.simpleName

    private var url: String? = null

    override suspend fun getSearchData(keyWord: String, page: Int): List<BaseData> {
        val data = mutableListOf<BaseData>()

        val offset = (page - 1) * 10
        url = "https://m.ixigua.com/video/m/search/search_content/?aid=3586&keyword=$keyWord&device_id=&offset=$offset&count=10"
        Log.d(TAG, "url=$url")

        val headers = Headers.Builder()
            .add("cookie", "ttwid=7225789753688655397;ixigua-a-s=0;")
            .build()
        val json = HttpUtils.syncGet(TAG, url, headers)
        val jsonObject = JSONObject(json)
        val message = jsonObject.getString("message")
        if ("success" == message) {
            val dataJSONArray = jsonObject.getJSONArray("data")
            val length = dataJSONArray.length()
            for (i in 0 until length) {
                val videoDataJSONObject = dataJSONArray[i] as JSONObject
                val groupId = videoDataJSONObject.optString("group_id")
                if (groupId == "") {
                    continue
                }
                val play_count = videoDataJSONObject.optLong("play_count")
                val duration = videoDataJSONObject.optInt("duration")
                val publishTime = videoDataJSONObject.optLong("publish_time", 0)
                val title = videoDataJSONObject.getString("title")
                val video_detail_info = videoDataJSONObject.optJSONObject("video_detail_info")
                val detail_video_large_image = video_detail_info?.optJSONObject("detail_video_large_image")
                val coverImageUrl = detail_video_large_image?.optString("url")

                val userInfoJSONObject =  videoDataJSONObject.getJSONObject("user_info")
                val userName = userInfoJSONObject.getString("name")
                val desc = videoDataJSONObject.optString("author_desc")
                val url = "https://m.ixigua.com/video/$groupId?title= " +
                        URLEncoder.encode(title, "UTF-8") +
                        "&cover=" +
                        URLEncoder.encode(coverImageUrl, "UTF-8") +
                        "&publishTime=$publishTime" +
                        "&play_count=$play_count" +
                        "&duration=$duration" +
                        "&name=$userName" +
                        "&desc=" +
                        URLEncoder.encode(desc, "UTF-8")

                data.add(MediaInfo2Data(title, coverImageUrl!!, "", "", userName).apply {
                    spanSize = 8
                    action = DetailAction.obtain(url)
                })
            }
        }

        return data
    }
}