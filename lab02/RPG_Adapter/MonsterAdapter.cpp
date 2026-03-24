#include "MonsterAdapter.h"
#include <iostream>

MonsterAdapter::MonsterAdapter(LegacyMonster* monster)
    : legacyMonster(monster)
{
}

MonsterAdapter::~MonsterAdapter()
{
    delete legacyMonster;
}

void MonsterAdapter::TakeDamage(int damage)
{
    std::cout << "[MonsterAdapter] TakeDamage() -> Hurt() - ADAPTING!\n";
    legacyMonster->Hurt(damage);
}

bool MonsterAdapter::IsAlive()
{
    bool isDead = legacyMonster->IsDead();
    return !isDead;
}

std::string MonsterAdapter::GetName()
{
    return legacyMonster->GetMonsterName();
}

int MonsterAdapter::GetHealth()
{
    return legacyMonster->GetHP();
}