package com.csci571.artsyapp.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

/**
 * A composable that renders text with Markdown-style links that can be clicked.
 * Links in format [link text](https://full-url) will be rendered as clickable links.
 *
 * @param text The Markdown text containing links
 * @param modifier Modifier to be applied to the text
 */
@Composable
fun MarkdownLinkText(
    text: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    val annotated = buildAnnotatedString {
        var lastIndex = 0
        val linkRegex = "\\[(.*?)\\]\\((.*?)\\)".toRegex()
        val matches = linkRegex.findAll(text)

        for (match in matches) {
            val linkText = match.groupValues[1]
            val linkUrl  = match.groupValues[2]
            val start    = match.range.first
            val end      = match.range.last + 1

            // Append non-link text up to this match
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            // Build a LinkAnnotation with styling and click behavior
            val linkAnnotation = LinkAnnotation.Url(
                linkUrl,
                TextLinkStyles(
                    SpanStyle(
                        color           = MaterialTheme.colorScheme.primary,
                        textDecoration  = TextDecoration.Underline
                    )
                )
            ) {
                // onClick of the link
                uriHandler.openUri((it as LinkAnnotation.Url).url)
            }

            // Append the link text wrapped in withLink
            withLink(linkAnnotation) {
                append(linkText)
            }

            lastIndex = end
        }

        // Append any remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    // Render as a normal Text composable
    Text(
        text     = annotated,
        modifier = modifier,
        style    = MaterialTheme.typography.bodyMedium
    )
}
