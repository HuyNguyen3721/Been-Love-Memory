package com.eco.beenlovememory.screen.start

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow

class JobLoadAds {
    private var progress = -1
    private var isShowAds = false
    private var delay = 20L

    private val coroutineScope by lazy { CoroutineScope(Dispatchers.IO) }
    private var loopingFlowJob: Job? = null
    private var loopingFlow = flow {
        while (true) {
            emit(Unit)
            delay(delay)
        }
    }

    fun setDelay(delay: Long) {
        this.delay = delay
    }

    fun startJob(jobProgress: ((Int) -> Unit)) {
        if (isShowAds()) {
            return
        }
        stopJob()
        jobProgress.invoke(progress)
        loopingFlowJob = coroutineScope.launch(Dispatchers.Main) {
            loopingFlow.collect {
                progress++
                jobProgress.invoke(progress)
            }
        }
    }

    fun stopJob() {
        progress = -1
        loopingFlowJob?.cancel()
    }

    fun isShowAds(): Boolean {
        return isShowAds
    }

    fun setAdsShowed(isShow: Boolean) {
        isShowAds = isShow
    }
}