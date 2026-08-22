package com.ssajudn.barebudget.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.nl.translate.TranslateLanguage
import com.ssajudn.barebudget.domain.usecase.TranslateTextUseCase
import com.ssajudn.barebudget.presentation.R
import com.ssajudn.barebudget.utils.LanguageManager
import kotlinx.coroutines.launch

/**
 * A Composable that displays user-generated content in its original language,
 * providing a subtle "See Translation" (Lihat Terjemahan) action button powered by ML Kit.
 */
@Composable
fun TranslatableUserText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    enabled: Boolean = true,
    useCase: TranslateTextUseCase = hiltViewModel<TranslationViewModel>().useCase
) {
    val context = LocalContext.current
    // Recompute current language on recomposition so in-app settings changes reflect immediately
    val currentAppLang = LanguageManager.getCurrentLanguageCode(context)
    val scope = rememberCoroutineScope()

    var isTranslated by remember { mutableStateOf(false) }
    var translatedText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val targetMlKitLang = if (currentAppLang == "id") {
        TranslateLanguage.INDONESIAN
    } else {
        TranslateLanguage.ENGLISH
    }

    val sourceMlKitLang = if (targetMlKitLang == TranslateLanguage.INDONESIAN) {
        TranslateLanguage.ENGLISH
    } else {
        TranslateLanguage.INDONESIAN
    }

    Column(modifier = modifier) {
        AnimatedContent(
            targetState = if (isTranslated && translatedText != null) translatedText!! else text,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "translatable_text_content"
        ) { displayContent ->
            Text(
                text = displayContent,
                style = style,
                color = color
            )
        }

        if (enabled && text.isNotBlank()) {
            Row(
                modifier = Modifier.padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.translating),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                } else {
                    TextButton(
                        onClick = {
                            if (isTranslated) {
                                isTranslated = false
                            } else {
                                if (translatedText != null) {
                                    isTranslated = true
                                } else {
                                    scope.launch {
                                        isLoading = true
                                        isError = false
                                        val result = useCase(
                                            text = text,
                                            sourceLang = sourceMlKitLang,
                                            targetLang = targetMlKitLang
                                        )
                                        isLoading = false
                                        result.onSuccess { translated ->
                                            translatedText = translated
                                            isTranslated = true
                                        }.onFailure {
                                            isError = true
                                        }
                                    }
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTranslated) {
                                stringResource(R.string.see_original)
                            } else if (isError) {
                                stringResource(R.string.translation_failed)
                            } else {
                                stringResource(R.string.see_translation)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
