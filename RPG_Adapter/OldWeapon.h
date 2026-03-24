#pragma once
#include <string>

// Старый класс оружия (из старой библиотеки)
// У него метод Strike() вместо Attack()
class OldWeapon
{
private:
    std::string weaponName;
    int damage;

public:
    OldWeapon(const std::string& name, int dmg);

    // СТАРЫЙ метод атаки (не совместим с IWeapon!)
    int Strike();

    // СТАРЫЙ метод получения имени
    std::string GetWeaponName();
};