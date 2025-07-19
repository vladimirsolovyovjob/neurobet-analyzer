package org.example.parser;

import org.json.JSONObject;
import java.util.List;

/**
 * Конвертирует JSON-объекты матчей в список объектов {@link Game}.
 * Использует {@link GameDataExtractor} для извлечения данных и наполняет список активных игр.
 */
public class JsonGameMapper {

    private final GameDataExtractor gameDataExtractor;

    public JsonGameMapper(GameDataExtractor gameDataExtractor) {
        this.gameDataExtractor = gameDataExtractor;
    }

    /**
     * Преобразует JSON-данные в список объектов {@link Game}.
     *
     * @param jsonObject JSON с матчами.
     * @param gameList   Список текущих матчей, который будет обновлён.
     */
    public void mapJsonToGames(JSONObject jsonObject, List<Game> gameList) {
        List<List<String>> extractedGameData = gameDataExtractor.processJson(jsonObject);

        for (List<String> dataRow : extractedGameData) {
            int gameId = Integer.parseInt(dataRow.get(5));
            boolean isNewGame = true;

            for (Game game : gameList) {
                if (game.getGameId() == gameId) {
                    updateGameFields(game, dataRow);
                    isNewGame = false;
                    break;
                }
            }

            if (isNewGame) {
                Game newGame = new Game(gameId);
                updateGameFields(newGame, dataRow);
                gameList.add(newGame);
            }
        }
    }

    /**
     * Присваивает значения из строки данных объекту {@link Game}.
     *
     * @param game    Игра, которую нужно обновить.
     * @param rawData Строка данных, извлечённая из JSON.
     */
    private void updateGameFields(Game game, List<String> rawData) {
        int time = Integer.parseInt(rawData.get(2));
        int scoreTeam1 = Integer.parseInt(rawData.get(3));
        int scoreTeam2 = Integer.parseInt(rawData.get(4));
        double proposedTotal = Double.parseDouble(rawData.get(6));

        game.setTime(time);
        game.setTeamName1(rawData.get(0));
        game.setTeamName2(rawData.get(1));

        switch (time / 60) {
            case 1 -> game.setMinute1(scoreTeam1, scoreTeam2, proposedTotal);
            case 2 -> game.setMinute2(scoreTeam1, scoreTeam2, proposedTotal);
            case 3 -> game.setMinute3(scoreTeam1, scoreTeam2, proposedTotal);
            case 4 -> game.setMinute4(scoreTeam1, scoreTeam2, proposedTotal);
            case 5 -> game.setMinute5(scoreTeam1, scoreTeam2, proposedTotal);
            case 6 -> game.setMinute6(scoreTeam1, scoreTeam2, proposedTotal);
            case 7 -> game.setMinute7(scoreTeam1, scoreTeam2, proposedTotal);
            case 8 -> game.setMinute8(scoreTeam1, scoreTeam2, proposedTotal);
            case 9 -> game.setMinute9(scoreTeam1, scoreTeam2, proposedTotal);
            default -> {
                // Если минута выходит за рамки 0–9, считаем это завершением четверти
                if (game.isAwaitingFinalResult() && time < 720) {
                    game.setFinalScore(score1 + score2);
                    game.setReadyToWrite(true);
                    game.setAwaitingFinalResult(false);
                }
            }
        }
    }
}
