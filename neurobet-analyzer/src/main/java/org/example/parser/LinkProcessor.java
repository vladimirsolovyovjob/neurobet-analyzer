package org.example.parser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * Класс отвечает за обработку и управление ссылками на игры.
 * Включает логику очистки устаревших ссылок и итерацию по активным.
 */
public class LinkProcessor {

    private static final Logger logger = Logger.getLogger(LinkProcessor.class.getName());

    private final int trackingArraySize = 1000;
    private final ConcurrentMap<Integer, Long> linkMap = new ConcurrentHashMap<>();
    private final Integer[] trackingArray = new Integer[trackingArraySize];

    private int cursor = 0;
    private Iterator<Integer> iterator;

    /**
     * Обрабатывает полученную ссылку.
     * Добавляет в map и массив, если ссылка новая.
     *
     * @param linkId идентификатор игры (ссылки).
     */
    public void registerLink(Integer linkId) {
        logger.info("Получена ссылка: " + linkId);

        if (linkId == null) {
            logger.warning("Передана null-ссылка. Пропуск.");
            return;
        }

        // Удаляем устаревшие ссылки старше 20 минут
        for (Map.Entry<Integer, Long> entry : linkMap.entrySet()) {
            if (System.currentTimeMillis() - entry.getValue() > 20 * 60 * 1000) {
                logger.info("Удалена устаревшая ссылка: " + entry.getKey());
                linkMap.remove(entry.getKey());
            }
        }

        if (!isInTrackingArray(linkId)) {
            linkMap.put(linkId, System.currentTimeMillis());
            trackingArray[cursor] = linkId;
            cursor = (cursor + 1) % trackingArraySize; // Циклический индекс
        }
    }


    /**
     * Возвращает строку запроса с одним идентификатором игры.
     * Автоматически реинициализирует итератор при завершении или очистке map.
     *
     * @return строка запроса вида "&subGames=12345" или пустая строка при отсутствии активных ссылок.
     */
    public String getLink() {
        logger.info("Попытка извлечения активной ссылки из map...");

        try {
            if (iterator == null || !iterator.hasNext()) {
                iterator = linkMap.keySet().iterator();
            }

            if (iterator.hasNext()) {
                Integer linkId = iterator.next();
                String result = "&subGames=" + linkId;
                logger.info("Возвращена активная ссылка: " + result);
                return result;
            }

        } catch (Exception e) {
            logger.warning("Ошибка при попытке получить ссылку: " + e.getMessage());
        }

        logger.info("Нет доступных ссылок для отправки.");
        return "";
    }
}