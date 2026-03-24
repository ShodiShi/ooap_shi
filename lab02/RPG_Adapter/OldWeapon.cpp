#include "OldWeapon.h"

OldWeapon::OldWeapon(const std::string& name, int dmg)
    : weaponName(name), damage(dmg)
{
}

int OldWeapon::Strike()
{
    // СТАРЫЙ метод - называется Strike, а не Attack
    return damage;
}

std::string OldWeapon::GetWeaponName()
{
    // СТАРЫЙ метод - GetWeaponName, а не GetName
    return weaponName;
}