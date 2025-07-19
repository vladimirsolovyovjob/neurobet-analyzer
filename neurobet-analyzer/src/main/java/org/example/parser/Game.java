package org.example.parser;

import java.util.Objects;

/**
 * Представляет одну распарсенную спортивную игру со всеми минутными счетами,
 * тоталами от букмекера и управляющими флагами для обучения и предсказания.
 *
 * <p>Каждый экземпляр хранит данные о результатах команд по первым 9 минутам
 * баскетбольного матча (с интервалом в 1 минуту), предложенные букмекером тоталы
 * и итоговый суммарный результат для обучения с учителем.</p>
 */

public class Game {

    private final int gameId;

    private String teamOne;
    private String teamTwo;

    private int gameTimeInSeconds;
    private long lastUpdateTimestamp = System.currentTimeMillis();

    private int[] teamOneScores = new int[9];
    private int[] teamTwoScores = new int[9];
    private double[] bookmakerTotals = new double[9];

    private boolean[] lockMinute = new boolean[]{true, true, true, true, true, true, true, true, true};

    private int finalTotalScore;
    private boolean finalTotalLocked = true;
    private boolean readyToWrite = false;
    private long selfDestructTimestamp = 2_000_000_000_000L;

    public Game(int gameId) {
        this.gameId = gameId;
    }

    public int getGameId() {
        return gameId;
    }

    public String getTeamOne() {
        return teamOne;
    }

    public void setTeamOne(String teamOne) {
        this.teamOne = teamOne;
    }

    public String getTeamTwo() {
        return teamTwo;
    }

    public void setTeamTwo(String teamTwo) {
        this.teamTwo = teamTwo;
    }

    public int getGameTimeInSeconds() {
        return gameTimeInSeconds;
    }

    public void setGameTimeInSeconds(int gameTimeInSeconds) {
        this.gameTimeInSeconds = gameTimeInSeconds;
    }

    public int getScoreForTeamOneAtMinute(int minuteIndex) {
        return teamOneScores[minuteIndex];
    }

    public void setScoreForTeamOneAtMinute(int minuteIndex, int score) {
        teamOneScores[minuteIndex] = score;
    }

    public int getScoreForTeamTwoAtMinute(int minuteIndex) {
        return teamTwoScores[minuteIndex];
    }

    public void setScoreForTeamTwoAtMinute(int minuteIndex, int score) {
        teamTwoScores[minuteIndex] = score;
    }

    public double getBookmakerTotalAtMinute(int minuteIndex) {
        return bookmakerTotals[minuteIndex];
    }

    public void setBookmakerTotalAtMinute(int minuteIndex, double total) {
        bookmakerTotals[minuteIndex] = total;
    }

    public boolean isMinuteLocked(int minuteIndex) {
        return lockMinute[minuteIndex];
    }

    public void unlockMinute(int minuteIndex) {
        lockMinute[minuteIndex] = false;
    }

    public int getFinalTotalScore() {
        return finalTotalScore;
    }

    public void setFinalTotalScore(int finalTotalScore) {
        this.finalTotalScore = finalTotalScore;
    }

    public boolean isFinalTotalLocked() {
        return finalTotalLocked;
    }

    public void unlockFinalTotal() {
        this.finalTotalLocked = false;
    }

    public boolean isReadyToWrite() {
        return readyToWrite;
    }

    public void setReadyToWrite(boolean readyToWrite) {
        this.readyToWrite = readyToWrite;
    }

    public long getSelfDestructTimestamp() {
        return selfDestructTimestamp;
    }

    public void setSelfDestructTimestamp(long selfDestructTimestamp) {
        this.selfDestructTimestamp = selfDestructTimestamp;
    }

    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void updateLastInteraction() {
        this.lastUpdateTimestamp = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Game game = (Game) obj;
        return gameId == game.gameId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId);
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId=" + gameId +
                ", gameTimeInSeconds=" + gameTimeInSeconds +
                ", finalTotalScore=" + finalTotalScore +
                ", readyToWrite=" + readyToWrite +
                '}';
    }
}
