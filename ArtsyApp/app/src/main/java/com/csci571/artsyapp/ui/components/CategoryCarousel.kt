package com.csci571.artsyapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale

import coil3.compose.AsyncImage
import com.csci571.artsyapp.data.model.Category
import kotlinx.coroutines.launch

/**
 * 类别轮播组件
 * 显示一组类别的轮播效果
 *
 * @param categories 要显示的类别列表
 * @param modifier 应用于轮播的修饰符
 */
@Composable
fun CategoryCarousel(
    categories: List<Category>,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    // 创建并记忆PagerState
    val pagerState = rememberPagerState(initialPage = 0) { categories.size }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxWidth()) {
        // 使用HorizontalPager实现轮播
        HorizontalPager(
            state = pagerState,
            // 设置内边距使相邻卡片的边缘可见
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) { page ->
            // 显示当前页的卡片
            CategoryCard(
                category = categories[page],
                modifier = Modifier
                    .padding(4.dp)
            )
        }

        // 左箭头 - 循环到上一个
        IconButton(
            onClick = {
                coroutineScope.launch {
                    // 计算上一页，如果是第一页则循环到最后一页
                    val prevPage = if (pagerState.currentPage > 0)
                        pagerState.currentPage - 1
                    else
                        categories.size - 1
                    pagerState.animateScrollToPage(prevPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-8).dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "上一个",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 右箭头 - 循环到下一个
        IconButton(
            onClick = {
                coroutineScope.launch {
                    // 计算下一页，如果是最后一页则循环到第一页
                    val nextPage = if (pagerState.currentPage < categories.size - 1)
                        pagerState.currentPage + 1
                    else
                        0
                    pagerState.animateScrollToPage(nextPage)
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 8.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "下一个",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

/**
 * 类别卡片组件
 * 显示单个类别的信息
 *
 * @param category 要显示的类别
 * @param modifier 应用于卡片的修饰符
 */
@Composable
fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier
) {
    // 创建一个滚动状态用于描述部分
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 图片部分
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            // 类别名称
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp)
            )

            // 使用MarkdownLinkText组件显示类别描述
            // 后端已将链接中的相对路径转为完整URL，这里只需解析Markdown格式
            MarkdownLinkText(
                text = category.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)  // 添加垂直滚动功能
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

        }
    }
}