#pragma once
#include <string>

// Интерфейс современного оружия
class IWeapon
{
public:
    virtual ~IWeapon() = default;

    // Современный метод атаки
    virtual int Attack() = 0;

    // Получить название оружия
    virtual std::string GetName() = 0;
};