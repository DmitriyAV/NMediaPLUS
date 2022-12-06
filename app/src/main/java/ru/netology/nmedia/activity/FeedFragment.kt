package ru.netology.nmedia.activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.CardPostFragment.Companion.showPost
import ru.netology.nmedia.adapter.FeedAdapter
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    @Inject lateinit var postsApiService: PostsApiService
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private val authViewModel: AuthViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = FeedAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                when (post.likedByMe) {
                    true -> viewModel.disLikeById(post.id)
                    false -> viewModel.likeById(post.id)
                }
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onPlayVideo(post: Post) {
                val videoIntent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(videoIntent)
            }

            override fun onSinglePost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_cardPostFragment2,
                    Bundle().apply
                    {
                        showPost = post
                    })
            }

            override fun onFullScreenImage(post: Post) {
                TODO("Not yet implemented")
            }
        })

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            with(binding) {
                progress.isVisible = state.loading
                swiperefresh.isRefreshing = state.refreshing
                if (state.error) {
                    errorGroup.isVisible = state.error
                    errorGroup.setOnClickListener {
                        viewModel.tryAgain()
                       errorGroup.visibility = View.GONE
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing = it.refresh is LoadState.Loading
                        || it.append is LoadState.Loading
                        || it.prepend is LoadState.Loading
            }
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        authViewModel.data.observe(viewLifecycleOwner) { adapter.refresh() }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

     /*   viewModel.newerPost.observe(viewLifecycleOwner) { state ->
            when (state) {
                1 -> binding.newPosts.visibility = View.VISIBLE
                else -> return@observe
            }
        }*/

        binding.newPosts.setOnClickListener {
            binding.newPosts.visibility = View.GONE
            viewModel.getUnreadPosts()
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadPosts()
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }
        return binding.root
    }
}
