package org.example.neuralnet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * Класс представляет собой реализацию простой двухслойной нейронной сети.
 * Используется для предсказания результата спортивного события (ставка или нет)
 * на основе бинарных входных данных размером 299 байт.
 *
 * Каждый входной нейрон получает бинарное значение из обучающего файла.
 * Затем данные проходят через скрытые нейроны и поступают на выходной нейрон.
 */
public class NeuralNetwork {

    private static final Logger logger = Logger.getLogger(NeuralNetwork.class.getName());

    private String[][] trainingDataCache;
    private boolean isDataCached = false;

    private List<Neuron> inputNeurons = new ArrayList<>();
    private List<Neuron> hiddenNeurons = new ArrayList<>();
    private Neuron outputNeuron = new Neuron();

    private double initialInputWeight;      // Начальные веса между входными и скрытыми нейронами
    public double initialHiddenWeight;      // Начальные веса между скрытыми и выходным нейронами
    private double learningRate;            // Скорость обучения
    private int trainingCycles;             // Количество итераций обучения

    public void setInitialWeight(double weight) {
        this.initialInputWeight = weight;
    }

    public void setLearningRate(double rate) {
        this.learningRate = rate;
    }

    public void setNumTrainingCycles(int cycles) {
        this.trainingCycles = cycles;
    }

    public void setInitialHiddenWeight(double initialHiddenWeight) {
        this.initialHiddenWeight = initialHiddenWeight;
    }

    /**
     * Инициализация структуры нейронной сети.
     * Входные и скрытые нейроны создаются в количестве 299.
     * Связи между ними и весовые коэффициенты задаются случайно.
     */
    public void initializeNeuralNetwork() {
        Random rnd = new Random(123); // Фиксированное зерно для повторяемости

        inputNeurons.clear();
        hiddenNeurons.clear();

        for (int i = 0; i < 299; i++) {
            inputNeurons.add(new Neuron());
            hiddenNeurons.add(new Neuron());
        }

        for (Neuron input : inputNeurons) {
            for (Neuron hidden : hiddenNeurons) {
                input.strelkaMap.put(hidden, rnd.nextDouble(-initialInputWeight, initialInputWeight));
            }
        }

        for (Neuron hidden : hiddenNeurons) {
            hidden.strelkaMap.put(outputNeuron, rnd.nextDouble(-initialHiddenWeight, initialHiddenWeight));
        }

        logger.info("Нейросеть инициализирована.");
    }

    /**
     * Задание входных значений нейронной сети.
     * @param values массив бинарных значений (0 или 1), размер — 299
     */
    public void setInputValues(byte[] values) {
        if (values.length != 299) {
            throw new IllegalArgumentException("Ожидалось 299 входных значений.");
        }

        for (int i = 0; i < 299; i++) {
            inputNeurons.get(i).value = values[i] != 0 ? 1 : 0;
        }
    }

    /**
     * Запуск нейросети на входных данных и возвращение результата ("Ставим" или "Отказываемся от ставки")
     */
    public String run(byte[] inputValues) {
        setInputValues(inputValues);
        double result = calc();
        return result > 0.5 ? "Ставим" : "Отказываемся от ставки";
    }

    /**
     * Обучение нейросети на указанном текстовом файле.
     * Каждая строка содержит 299 бинарных значений и 1 целевой результат.
     */
    public void training(String filePath) throws IOException {
        if (!isDataCached) {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            trainingDataCache = new String[lines.size()][];
            for (int i = 0; i < lines.size(); i++) {
                trainingDataCache[i] = lines.get(i).split(" ");
            }
            isDataCached = true;
            logger.info("Загружены данные из файла: " + filePath);
        }

        for (int cycle = 0; cycle < trainingCycles; cycle++) {
            for (String[] row : trainingDataCache) {
                if (row.length < 300) continue; // защита от пустых или кривых строк

                for (int i = 0; i < 299; i++) {
                    inputNeurons.get(i).value = Double.parseDouble(row[i]);
                }

                double expected = Double.parseDouble(row[299]);
                double actual = calc();
                if ((actual > 0.5 ? 1 : 0) != expected) {
                    adjustWeights(actual, expected);
                }
            }
        }

        logger.info("Обучение завершено.");
    }

    /**
     * Метод обратного распространения ошибки (backpropagation).
     */
    private void adjustWeights(double outputValue, double expectedValue) {
        double error = outputValue - expectedValue;
        double delta = error * (1 - error) * learningRate;

        for (Neuron hidden : hiddenNeurons) {
            double oldWeight = hidden.strelkaMap.get(outputNeuron);
            hidden.strelkaMap.put(outputNeuron, oldWeight - hidden.value * delta * initialHiddenWeight);
        }

        for (Neuron hidden : hiddenNeurons) {
            double error2 = hidden.strelkaMap.get(outputNeuron) * delta;
            double delta2 = error2 * (1 - error2) * learningRate;

            for (Neuron input : inputNeurons) {
                double oldWeight = input.strelkaMap.get(hidden);
                input.strelkaMap.put(hidden, oldWeight - input.value * delta2 * initialInputWeight);
            }
        }
    }

    /**
     * Вычисление значения выходного нейрона на основе входных данных.
     */
    private double calc() {
        for (Neuron hidden : hiddenNeurons) {
            double sum = 0;
            for (Neuron input : inputNeurons) {
                sum += input.value * input.strelkaMap.get(hidden);
            }
            hidden.value = sigmoid(sum);
        }

        double outputSum = 0;
        for (Neuron hidden : hiddenNeurons) {
            outputSum += hidden.value * hidden.strelkaMap.get(outputNeuron);
        }

        outputNeuron.value = sigmoid(outputSum);
        return outputNeuron.value;
    }

    /**
     * Сигмоидная функция активации.
     */
    private double sigmoid(double x) {
        return 1.0 / (1 + Math.exp(-x));
    }
}
