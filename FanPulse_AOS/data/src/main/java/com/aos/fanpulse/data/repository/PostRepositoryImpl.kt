package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.data.remote.PostRemoteDataSource
import com.aos.fanpulse.data.remote.dto.UserDto
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.repository.PostRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val remoteDataSource: PostRemoteDataSource
) : PostRepository {

    override suspend fun fetchPosts(artistCategory: String?, currentUserId: String): Result<List<Post>> {
        return try {
            val postDtos = remoteDataSource.getPosts(artistCategory)

            val posts = coroutineScope {
                postDtos.map { dto ->
                    async {
                        val isLikedDeferred = async { remoteDataSource.checkIsLiked(currentUserId, dto.id) }
                        val isBookmarkedDeferred = async { remoteDataSource.checkIsBookmarked(currentUserId, dto.id) }

                        dto.toDomain(
                            isLiked = isLikedDeferred.await(),
                            isBookmarked = isBookmarkedDeferred.await()
                        )
                    }
                }.awaitAll()
            }

            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPostDetail(postId: String, currentUserId: String): Result<Post> {
        return try {
            val dto = remoteDataSource.getPostById(postId)
                ?: throw NoSuchElementException("게시글이 존재하지 않습니다.")

            val post = coroutineScope {
                val isLikedDeferred = async { remoteDataSource.checkIsLiked(currentUserId, postId) }
                val isBookmarkedDeferred = async { remoteDataSource.checkIsBookmarked(currentUserId, postId) }

                dto.toDomain(
                    isLiked = isLikedDeferred.await(),
                    isBookmarked = isBookmarkedDeferred.await()
                )
            }

            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPost(
        postId: String,
        artistCategory: String,
        content: String,
        imageUrls: List<String>,
        tags: List<String>,
        authorId: String,
        authorNickname: String,
        authorProfileUrl: String,
        authorFandom: String?,
    ): Result<Unit> {
        return try {

            val postData = hashMapOf(
                "postId" to postId,
                "authorId" to authorId,
                "authorNickname" to authorNickname,
                "authorProfileUrl" to authorProfileUrl,
                "authorFandom" to authorFandom,
                "targetArtist" to artistCategory,
                "content" to content,
                "imageUrls" to imageUrls,
                "tags" to tags,
                "createdAt" to System.currentTimeMillis(),
                "likeCount" to 0,
                "commentCount" to 0,
                "shareCount" to 0
            )

            remoteDataSource.createPost(postData)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String, currentUserId: String): Result<Unit> {
        return try {
            coroutineScope {
                // 1. 원본 게시글 삭제
                val deletePostDeferred = async { remoteDataSource.deletePost(postId) }

                // 2. 현재 유저의 좋아요 정보 삭제 비동기 실행
                val deleteLikeDeferred = async { remoteDataSource.deleteLike(currentUserId, postId) }

                // 3. 현재 유저의 북마크 정보 삭제 비동기 실행
                val deleteBookmarkDeferred = async { remoteDataSource.deleteBookmark(currentUserId, postId) }

                // 모든 삭제 작업이 완료될 때까지 동시 대기 (awaitAll 활용 가능)
                awaitAll(deletePostDeferred, deleteLikeDeferred, deleteBookmarkDeferred)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}