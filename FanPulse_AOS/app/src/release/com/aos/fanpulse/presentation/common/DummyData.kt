import com.aos.fanpulse.data.remote.apiservice.Artist
import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsItem
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.data.remote.apiservice.SearchLiveItem
import com.aos.fanpulse.data.remote.apiservice.SearchNewsItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem

object DummyData {
    val streamingEventDummyList: List<StreamingEventItem> = emptyList()
    val streamingEventSimpleDummyList: List<StreamingEventSimpleItem> = emptyList()
    val newsItemDummyList: List<NewsItem> = emptyList()
    val newsDetailDummyList: List<NewsDetail> = emptyList()
    val artistDetailDummyList: List<ArtistDetail> = emptyList()
    val streamingEventDetailDummyList: List<StreamingEventDetail> = emptyList()
    val artistDummyList: List<Artist> = emptyList()
    val liveItems: List<SearchLiveItem> = emptyList()
    val newsItems: List<SearchNewsItem> = emptyList()

    val newsListResponseDummy = NewsListResponse(
        content = emptyList(),
        totalElements = 0,
        page = 0,
        size = 0,
        totalPages = 0
    )
}