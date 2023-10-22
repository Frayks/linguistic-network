package org.andyou.linguistic_network.lib;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ProgressBarProcessor {

    private final JProgressBar progressBar;
    private final Queue<Float> blockSizeQueue;
    private float blockSize;
    private float stepSize;
    private float progress;


    public ProgressBarProcessor(JProgressBar progressBar, List<Integer> blockSizes) {
        this.progressBar = progressBar;
        this.blockSizeQueue = new ArrayDeque<>();
        int blockSizesSum = blockSizes.stream().mapToInt(Integer::intValue).sum();
        int progressBarMax = progressBar.getMaximum();
        blockSizes.forEach(blockSize -> blockSizeQueue.add((float) blockSize / blockSizesSum * progressBarMax));
        progressBar.setValue(0);
    }

    public void initAndFinishNextBlock() {
        blockSize = blockSizeQueue.poll();
        progress += blockSize;
        updateProgressBar();
    }

    public void initNextBlock(int stepsCount) {
        blockSize = blockSizeQueue.poll();
        stepSize = 1.0f / stepsCount * blockSize;
    }

    public void walk() {
        progress += stepSize;
        updateProgressBar();
    }

    private void updateProgressBar() {
        progressBar.setValue(Math.round(progress));
    }

}
