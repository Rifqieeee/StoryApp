import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.intermediateapplication1.DataDummy
import com.example.intermediateapplication1.MainDispatcherRule
import com.example.intermediateapplication1.data.ListStoryItem
import com.example.intermediateapplication1.getOrAwaitValue
import com.example.intermediateapplication1.ui.story.StoryRepository
import com.example.intermediateapplication1.ui.story.StoryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var storyViewModel: StoryViewModel

    @Mock
    private lateinit var storyRepository: StoryRepository

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        storyViewModel = StoryViewModel(storyRepository)
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val emptyPagingData = PagingData.empty<ListStoryItem>()
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = emptyPagingData

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedStory)

        val actualStory = storyViewModel.listStory.getOrAwaitValue()
        assertNotNull(actualStory)

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItem.DIFF_CALLBACK,
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = mainDispatcherRule.testDispatcher
        )
        differ.submitData(actualStory)
        advanceUntilIdle()

        assertNotNull(differ.snapshot())
        assertEquals(0, differ.snapshot().size)
    }

    @Test
    fun `when Get Stories Should Not Be Null And Return Data`() = runTest {
        val dummyData = DataDummy.generateDummyStories()
        val expectedData: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyData)
        val expectedLiveData = MutableLiveData<PagingData<ListStoryItem>>()
        expectedLiveData.value = expectedData

        Mockito.`when`(storyRepository.getStories()).thenReturn(expectedLiveData)

        val actualData = storyViewModel.listStory.getOrAwaitValue()
        assertNotNull(actualData)

        val differ = AsyncPagingDataDiffer(
            diffCallback = ListStoryItem.DIFF_CALLBACK,
            updateCallback = NoopListUpdateCallback(),
            workerDispatcher = mainDispatcherRule.testDispatcher
        )
        differ.submitData(actualData)
        advanceUntilIdle()

        assertNotNull(differ.snapshot())
        assertEquals(dummyData.size, differ.snapshot().size)
        assertEquals(dummyData[0], differ.snapshot()[0])
    }

    @Test
    fun `when Refresh Stories Should Call Invalidate Paging`() = runTest {
        storyViewModel.refreshStories()
        Mockito.verify(storyRepository).invalidatePaging()
    }

    class NoopListUpdateCallback : ListUpdateCallback {
        override fun onInserted(position: Int, count: Int) = Unit
        override fun onRemoved(position: Int, count: Int) = Unit
        override fun onMoved(fromPosition: Int, toPosition: Int) = Unit
        override fun onChanged(position: Int, count: Int, payload: Any?) = Unit
    }

    class StoryPagingSource : PagingSource<Int, ListStoryItem>() {
        companion object {
            fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
                return PagingData.from(items)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
            return state.anchorPosition
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
            return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
        }
    }
}
