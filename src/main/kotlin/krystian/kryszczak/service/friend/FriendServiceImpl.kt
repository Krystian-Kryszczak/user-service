package krystian.kryszczak.service.friend

import jakarta.inject.Singleton
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao

@Singleton
class FriendServiceImpl(private val friendDao: FriendDao): FriendService
