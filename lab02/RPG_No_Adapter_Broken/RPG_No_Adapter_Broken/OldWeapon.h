#pragma once
#include <string>

class OldWeapon {
public:
    int Strike() { return 15; } // Старый метод удара
    std::string GetWeaponName() { return "Rusty Old Sword"; }
};