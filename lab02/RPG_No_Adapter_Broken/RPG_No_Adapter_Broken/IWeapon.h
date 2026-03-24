#pragma once
#include <string>

// Это "Стандарт", которому должно соответствовать любое оружие в игре
class IWeapon {
public:
    // Виртуальный деструктор (чтобы память очищалась правильно)
    virtual ~IWeapon() = default;

    // Чисто виртуальные методы (интерфейс)
    virtual int Attack() = 0;          // Метод для нанесения урона
    virtual std::string GetName() = 0;  // Метод для получения названия
};