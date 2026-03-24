#include "ModernBow.h"
#include <iostream>

ModernBow::ModernBow(int dmg)
    : damage(dmg)
{
}

int ModernBow::Attack()
{
    std::cout << "[ModernBow] Direct Attack() call - NO ADAPTER NEEDED!\n";
    return damage;
}

std::string ModernBow::GetName()
{
    return "Modern Bow";
}