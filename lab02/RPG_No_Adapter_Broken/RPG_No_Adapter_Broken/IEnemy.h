#pragma once
#include <string>

class IEnemy {
public:
    virtual ~IEnemy() = default;
    virtual void TakeDamage(int damage) = 0; // Метод, который ждет игра
    virtual bool IsAlive() = 0;
};