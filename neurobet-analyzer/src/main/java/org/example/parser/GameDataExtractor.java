package org.example.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Основной исполняемый класс, отвечающий за извлечение информации из JSON-ответов
 * и подготовку данных для последующей обработки или обучения нейросети.
 */
public class GameDataExtractor {

    private static final Logger LOGGER = Logger.getLogger(GameDataExtractor.class.getName());

    private final LinkProcessor linkProcessor;
    private final Map<Integer, double[]> coefficientMap;

    public GameDataExtractor(LinkProcessor linkProcessor, Map<Integer, double[]> coefficientMap) {
        this.linkProcessor = linkProcessor;
        this.coefficientMap = coefficientMap;
    }

    /**
     * Обрабатывает JSON-объект с играми и извлекает нужные параметры.
     *
     * @param jsonObject JSON-объект с играми
     * @return Список игр, каждая из которых представлена списком параметров
     */
    public List<List<String>> processJson(JSONObject jsonObject) {
        List<List<String>> allGameData = new ArrayList<>();
        int gameTime = 0;

        JSONArray gameList = jsonObject.optJSONArray("Value");
        if (gameList != null) {
            for (int i = 0; i < gameList.length(); i++) {
                JSONObject gameObject = gameList.getJSONObject(i);
                List<String> gameData = new ArrayList<>();

                // Счёт команд
                String teamOneScore = gameObject.optString("O1", "0");
                String teamTwoScore = gameObject.optString("O2", "0");
                gameData.add(teamOneScore);
                gameData.add(teamTwoScore);

                JSONObject scObject = gameObject.optJSONObject("SC");
                boolean totalExtracted = false;
                int s1 = 0, s2 = 0;

                if (scObject != null) {
                    JSONArray periodScores = scObject.optJSONArray("PS");
                    if (periodScores != null) {
                        for (int j = 0; j < periodScores.length(); j++) {
                            JSONObject psObj = periodScores.getJSONObject(j);
                            String periodName = psObj.optString("NF", "");
                            s1 = psObj.optInt("S1", 100);
                            s2 = psObj.optInt("S2", 100);

                            if ("1-я Четверть".equals(periodName)) {
                                gameData.add(String.valueOf(s1));
                                gameData.add(String.valueOf(s2));
                                break;
                            }
                        }
                    }

                    String ts = scObject.optString("TS", "1500");
                    gameData.add(ts);
                    gameTime = Integer.parseInt(ts);

                    JSONObject fullScore = scObject.optJSONObject("FS");
                    if (fullScore != null) {
                        s1 = fullScore.optInt("S1", 100);
                        s2 = fullScore.optInt("S2", 100);
                        gameData.add(String.valueOf(s1));
                        gameData.add(String.valueOf(s2));
                    }

                    int serialKey = extractSerialKey(gameObject);
                    gameData.add(String.valueOf(serialKey));

                    totalExtracted = extractTotalFromJson(gameObject, gameData, gameTime);
                }

                // Условия добавления игры в итоговый список
                String currentPeriod = scObject != null ? scObject.optString("CPS", "") : "";
                if ("1-я Четверть".equals(currentPeriod) && gameTime > 60) {
                    if (totalExtracted && s1 != 100 && s2 != 100) {
                        allGameData.add(gameData);
                    }
                } else if (gameTime >= 600 && gameTime < 720 && s1 != 100 && s2 != 100) {
                    gameData.add("14.3");
                    allGameData.add(gameData);
                }

                // Обработка URL игры
                int gameId = gameObject.optInt("I");
                linkProcessor.prepareUrl(gameId);
            }
        }

        return allGameData;
    }

    /**
     * Извлекает информацию о тотале из блока SG → E в JSON.
     *
     * @param jsonObject объект игры
     * @param gameData список параметров для одной игры
     * @param timestamp время игры (TS)
     * @return true — если удалось извлечь тотал
     */
    public boolean extractTotalFromJson(JSONObject jsonObject, List<String> gameData, int timestamp) {
        try {
            JSONArray sgArray = jsonObject.getJSONArray("SG");
            for (int i = 0; i < sgArray.length(); i++) {
                JSONObject sgObj = sgArray.getJSONObject(i);
                String quarterName = sgObj.optString("PN", "");

                JSONArray eArray = sgObj.getJSONArray("E");
                for (int j = 0; j < eArray.length(); j++) {
                    JSONObject eObj = eArray.getJSONObject(j);

                    double total = eObj.getDouble("C");
                    int type = eObj.getInt("T");

                    if ("1-я Четверть".equals(quarterName) && type == 9 && timestamp > 60 && timestamp <= 600) {
                        gameData.add(String.valueOf(total));
                        return true;
                    } else if (timestamp >= 600 && timestamp < 720) {
                        gameData.add("11.0");
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "Ошибка извлечения тотала из JSON: {0}", e.getMessage());
        }
        return false;
    }

    /**
     * Извлекает уникальный серийный ключ игры.
     *
     * @param json объект игры
     * @return серийный ключ игры или 0, если он не найден
     */
    public int extractSerialKey(JSONObject json) {
        if (json.has("O2IS")) {
            JSONArray array = json.getJSONArray("O2IS");
            if (array.length() > 0) {
                return array.optInt(0);
            }
        }
        return 0;
    }
}
