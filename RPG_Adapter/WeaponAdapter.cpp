#include "WeaponAdapter.h"
#include <iostream>

WeaponAdapter::WeaponAdapter(OldWeapon* weapon)
    : oldWeapon(weapon)
{
}

WeaponAdapter::~WeaponAdapter()
{
    delete oldWeapon;
}

int WeaponAdapter::Attack()
{
    std::cout << "[WeaponAdapter] Attack() -> Strike() - ADAPTING!\n";
    return oldWeapon->Strike();
}

std::string WeaponAdapter::GetName()
{
    return oldWeapon->GetWeaponName();
}