package ru.vsu.cs.cg.pronin_s_v.task1_primitives.core;

/**
 * Интерфейс: Динамический объект.
 */
public interface Dynamic {
    /**
     * @param deltaSeconds прошедшее время с прошлого кадра, в секундах
     */
    void update(double deltaSeconds);
}