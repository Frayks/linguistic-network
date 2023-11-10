package org.andyou.linguistic_network.lib.gui;

import javax.swing.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ProgressBarProcessor {

    private final JProgressBar progressBar;
    private final Queue<Double> blockSizeQueue;
    private double blockSize;
    private double stepSize;
    private double progress;


    public ProgressBarProcessor(JProgressBar progressBar, List<Integer> blockSizes) {
        this.progressBar = progressBar;
        this.blockSizeQueue = new ArrayDeque<>();
        int blockSizesSum = blockSizes.stream().mapToInt(Integer::intValue).sum();
        int progressBarMax = progressBar.getMaximum();
        blockSizes.forEach(blockSize -> blockSizeQueue.add((double) blockSize / blockSizesSum * progressBarMax));
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

    synchronized public void walk() {
        progress += stepSize;
        updateProgressBar();
    }

    private void updateProgressBar() {
        progressBar.setValue((int) Math.round(progress));
    }

    public void completed() {
        progressBar.setValue(0);
    }

}
