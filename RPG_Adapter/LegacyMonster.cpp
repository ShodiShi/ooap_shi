#include "LegacyMonster.h"

LegacyMonster::LegacyMonster(const std::string& name, int hp)
    : monsterName(name), health(hp)
{
}

void LegacyMonster::Hurt(int damage)
{
    health -= damage;
    if (health < 0) health = 0;
}

bool LegacyMonster::IsDead()
{
    return health <= 0;
}

std::string LegacyMonster::GetMonsterName()
{
    return monsterName;
}

int LegacyMonster::GetHP()
{
    return health;
}