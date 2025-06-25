package com.android.swingmusic.folder.presentation.util

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.android.swingmusic.folder.presentation.model.FolderContentItem

inline fun LazyListScope.pagingFolderContent(
    items: LazyPagingItems<FolderContentItem>,
    crossinline itemContent: @Composable LazyItemScope.(item: FolderContentItem?) -> Unit
) {
    items(count = items.itemCount) { index ->
        itemContent(items[index])
    }
}