package com.ssajudn.barebudget.ui.components

import androidx.lifecycle.ViewModel
import com.ssajudn.barebudget.domain.usecase.TranslateTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TranslationViewModel @Inject constructor(
    val useCase: TranslateTextUseCase
) : ViewModel()
