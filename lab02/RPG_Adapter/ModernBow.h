#pragma once
#include "IWeapon.h"

// Современное оружие - напрямую реализует IWeapon
class ModernBow : public IWeapon
{
private:
    int damage;

public:
    ModernBow(int dmg);

    // Сразу реализует современный интерфейс
    int Attack() override;
    std::string GetName() override;
};