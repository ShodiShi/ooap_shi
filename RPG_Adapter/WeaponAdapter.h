#pragma once
#include "IWeapon.h"
#include "OldWeapon.h"

// АДАПТЕР: адаптирует OldWeapon под интерфейс IWeapon
class WeaponAdapter : public IWeapon
{
private:
    OldWeapon* oldWeapon;  // Старое оружие внутри адаптера

public:
    WeaponAdapter(OldWeapon* weapon);
    ~WeaponAdapter();

    // Реализуем современный интерфейс IWeapon
    int Attack() override;
    std::string GetName() override;
};