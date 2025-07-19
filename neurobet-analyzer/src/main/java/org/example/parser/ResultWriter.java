package org.example.parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Класс {@code ResultWriter} предназначен для:
 * <ul>
 *     <li>Формирования человекочитаемого лог-файла о результатах игры.</li>
 *     <li>Генерации векторного представления входных данных нейросети на основе игры.</li>
 *     <li>Записи этих данных в текстовый файл для последующего обучения.</li>
 * </ul>
 *
 * <h2>Принцип работы:</h2>
 * <ol>
 *     <li>В метод {@code writeGameResult(Game game)} поступает объект игры с заполненными параметрами (счета, тоталы, замки, результат и т.п.).</li>
 *     <li>Формируется строка-резюме об игре и записывается в лог.</li>
 *     <li>Для каждой минуты (1–9) вызывается {@code encodeMinute(...)} — метод формирует массив длиной 300 байт,
 *     где активированы ячейки, соответствующие:
 *         <ul>
 *             <li>Очкам команд</li>
 *             <li>Предложенному тоталу от букмекера (шаг 0.5)</li>
 *             <li>Минуте игры</li>
 *             <li>Итоговому исходу (победа или нет)</li>
 *         </ul>
 *     </li>
 *     <li>Каждая строка (вектор признаков) сохраняется построчно в файл output.txt</li>
 * </ol>
 *
 * <p>Вектор кодируется следующим образом:
 * <ul>
 *     <li>Ячейки [0..49] — очки первой команды</li>
 *     <li>Ячейки [50..99] — очки второй команды</li>
 *     <li>Ячейки [100..289] — значение тотала с шагом 0.5 (0.0..94.5), каждое значение умножается на 2</li>
 *     <li>Ячейки [290..298] — минута игры (1–9), сдвинутая на 289</li>
 *     <li>Ячейка 299 — бинарный исход: 1, если результат меньше предложенного тотала; иначе 0</li>
 * </ul>
 *
 * <p>Файл output.txt содержит:
 * <ul>
 *     <li>Лог событий с отметками времени</li>
 *     <li>Векторные строки — одна на каждую минуту игры</li>
 * </ul>
 *
 * <p>Для каждого нового запуска программы метод {@link #logStartupMarker()} фиксирует время старта.
 *
 * @author TG @DeciplineFX
 * @version 1.0
 * @since 2025-07-19
 */
public class ResultWriter {

    private static final Logger logger = Logger.getLogger(ResultWriter.class.getName());
    private static final String OUTPUT_PATH = "C:/output.txt";

    /**
     * Записывает результат игры в лог и сохраняет векторные представления в файл.
     *
     * @param game объект Game с заполненными полями.
     */
    public void writeGameResult(Game game) {
        logger.info("Запись информации об игре...");

        String summary = buildSummaryString(game);
        writeToFile(getTimestamp() + " - " + summary);

        logger.info(summary);
        logger.info("Начинается генерация строк для нейросети...");

        String[] minuteData = new String[]{
                byteArrayToString(encodeMinute(game.getTotalOne1(), game.getTotalTwo1(), game.getSuggestedTotal1Min(), 1, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne2(), game.getTotalTwo2(), game.getSuggestedTotal2Min(), 2, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne3(), game.getTotalTwo3(), game.getSuggestedTotal3Min(), 3, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne4(), game.getTotalTwo4(), game.getSuggestedTotal4Min(), 4, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne5(), game.getTotalTwo5(), game.getSuggestedTotal5Min(), 5, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne6(), game.getTotalTwo6(), game.getSuggestedTotal6Min(), 6, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne7(), game.getTotalTwo7(), game.getSuggestedTotal7Min(), 7, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne8(), game.getTotalTwo8(), game.getSuggestedTotal8Min(), 8, game.getFinalScore())),
                byteArrayToString(encodeMinute(game.getTotalOne9(), game.getTotalTwo9(), game.getSuggestedTotal9Min(), 9, game.getFinalScore()))
        };

        for (String line : minuteData) {
            writeToFile(line);
        }

        logger.info("Формирование завершено");
    }

    /**
     * Выводит отладочную метку старта программы в лог.
     */
    public void logStartupMarker() {
        writeToFile(getTimestamp() + " - Программа запущена.");
    }

    // —————————————————————————————————————————————— ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ————————————————————————————————————————————— //

    private String buildSummaryString(Game game) {
        return String.format(
                "Game #%d (%s vs %s): %n" +
                        "Tot1-1=%d, Tot1-2=%d, ..., Tot1-9=%d %n" +
                        "Tot2-1=%d, Tot2-2=%d, ..., Tot2-9=%d %n" +
                        "Suggested-1=%.1f, ..., Suggested-9=%.1f %n" +
                        "Locks: [%b,%b,%b,%b,%b,%b,%b,%b,%b] ResultLock=%b, WriteLock=%b Time=%d",
                game.getId(),
                game.getTeamOne(), game.getTeamTwo(),
                game.getTotalOne1(), game.getTotalOne2(), game.getTotalOne9(),
                game.getTotalTwo1(), game.getTotalTwo2(), game.getTotalTwo9(),
                game.getSuggestedTotal1Min(), game.getSuggestedTotal9Min(),
                game.isLocked1(), game.isLocked2(), game.isLocked3(), game.isLocked4(),
                game.isLocked5(), game.isLocked6(), game.isLocked7(), game.isLocked8(), game.isLocked9(),
                game.isResultLocked(), game.isWriteLocked(), game.getCurrentTimeSeconds()
        );
    }

    private byte[] encodeMinute(int t1, int t2, double suggested, int minute, int finalTotal) {
        byte[] vector = new byte[300];

        // Безопасная проверка границ (max 49 очков на команду, макс тотал 94.5)
        if (isSafeIndex(t1, 0, 49)) vector[t1] = 1;
        if (isSafeIndex(t2 + 50, 50, 99)) vector[t2 + 50] = 1;

        int totalIndex = (int) (suggested * 2) + 100; // 0.5 шаг
        if (isSafeIndex(totalIndex, 100, 289)) vector[totalIndex] = 1;

        int minuteIndex = 289 + minute;
        if (isSafeIndex(minuteIndex, 289, 297)) vector[minuteIndex] = 1;

        vector[299] = (byte) (finalTotal < suggested ? 1 : 0);

        return vector;
    }

    private boolean isSafeIndex(int index, int min, int max) {
        return index >= min && index <= max;
    }

    private String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(b).append(' ');
        }
        return sb.toString().trim();
    }

    private void writeToFile(String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_PATH, true))) {
            writer.write(content);
            writer.newLine();
        } catch (IOException e) {
            logger.severe("Ошибка записи в файл: " + e.getMessage());
        }
    }

    private String getTimestamp() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}
