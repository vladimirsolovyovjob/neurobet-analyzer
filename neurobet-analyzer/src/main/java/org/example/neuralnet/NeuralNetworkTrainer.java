package org.example.neuralnet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.*;

/**
 * Класс для настройки нейросети путем подбора различных комбинаций параметров:
 * начальный вес, скорость обучения и вес между скрытым и выходным слоем.
 * 
 * Производится тренировка и оценка качества предсказаний по валидационной выборке.
 * 
 * Автор: Соловьев Владимир Николаевич, 01.03.1989
 */
public class NeuralNetworkTrainer {

    private static final Logger LOGGER = Logger.getLogger(NeuralNetworkTrainer.class.getName());

    static {
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.setUseParentHandlers(false);
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.INFO);
    }

    public static void main(String[] args) throws IOException {

        List<Double> successfulScores = new ArrayList<>();
        NeuralNetwork neuralNetwork = new NeuralNetwork();

        // Загрузка валидационных данных из файла
        List<String> validationLines = Files.readAllLines(Paths.get("C:/basketball_training_set.txt"));
        int totalLines = validationLines.size();

        // === Перебираемые параметры ===
        
        // Массив начальных весов между входным и скрытым слоями
        double[] possibleInitialWeights = {0.001, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        // Массив скоростей обучения (learning rate)
        double[] possibleLearningRates = {0.1, 0.2, 0.09, 0.08, 0.07, 0.06, 0.05, 0.04, 0.03, 0.02, 0.01};

        // Массив весов между скрытым и выходным слоями (второй уровень)
        double[] possibleHiddenToOutputWeights = {0.001, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        int trainingCycles = 100;

        long startTime = System.currentTimeMillis();
        long checkpointTime = startTime;

        LOGGER.info("Запуск процесса настройки нейросети...");

        neuralNetwork.setNumTrainingCycles(trainingCycles);

        for (double inputWeight : possibleInitialWeights) {
            for (double learningRate : possibleLearningRates) {
                for (double hiddenWeight : possibleHiddenToOutputWeights) {

                    // Установка параметров нейросети
                    neuralNetwork.setInitialWeight(inputWeight);
                    neuralNetwork.setLearningRate(learningRate);
                    neuralNetwork.setInitialHiddenWeight(hiddenWeight);

                    // Инициализация и тренировка
                    neuralNetwork.initializeNeuralNetwork();
                    neuralNetwork.training("C:/basketball_training_set.txt");

                    int correctCount = 0;

                    for (String line : validationLines) {
                        byte[] input = new byte[299];
                        for (int i = 0; i < line.length() - 1; i++) {
                            input[i] = Byte.parseByte(String.valueOf(line.charAt(i)));
                        }
                        byte result = (byte) line.charAt(line.length() - 1);

                        String prediction = neuralNetwork.run(input);

                        if ((prediction.equals("Ставим") && result == '1') ||
                            (prediction.equals("Отказываемся от ставки") && result == '0')) {
                            correctCount++;
                        }
                    }

                    double accuracy = (correctCount / (double) totalLines) * 100;

                    LOGGER.info(String.format(
                            "Параметры: входной вес = %.3f, скорость обучения = %.3f, скрытый→выходной = %.3f | Точность: %.2f%%",
                            inputWeight, learningRate, hiddenWeight, accuracy));

                    if (accuracy > 70.0) {
                        LOGGER.info("🎯 ВЫСОКАЯ ТОЧНОСТЬ: " + accuracy);
                        successfulScores.add(accuracy);
                    } else if (accuracy > 62.0) {
                        LOGGER.info("✅ Целевая точность достигнута: " + accuracy);
                        successfulScores.add(accuracy);
                    }
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - checkpointTime >= 6 * 60 * 60 * 1000) {
                    // Сортировка и промежуточный вывод каждые 6 часов
                    Collections.sort(successfulScores);
                    LOGGER.info("⏳ Промежуточные успешные точности: " + successfulScores);
                    checkpointTime = currentTime;
                }
            }
        }

        Collections.sort(successfulScores);
        LOGGER.info("🎉 Финальные лучшие результаты: " + successfulScores);

        long endTime = System.currentTimeMillis();
        long executionTimeMillis = endTime - startTime;
        long minutes = (executionTimeMillis / 1000) / 60;
        long seconds = (executionTimeMillis / 1000) % 60;

        LOGGER.info(String.format("⏱ Общее время выполнения: %d мин %d сек", minutes, seconds));
    }
}
