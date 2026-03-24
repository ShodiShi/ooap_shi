#pragma once
#include <string>

// Интерфейс современного врага
class IEnemy
{
public:
    virtual ~IEnemy() = default;

    // Современные методы
    virtual void TakeDamage(int damage) = 0;
    virtual bool IsAlive() = 0;
    virtual std::string GetName() = 0;
    virtual int GetHealth() = 0;
};