#pragma once
#include "IEnemy.h"
#include "LegacyMonster.h"

// АДАПТЕР: адаптирует LegacyMonster под интерфейс IEnemy
class MonsterAdapter : public IEnemy
{
private:
    LegacyMonster* legacyMonster;  // Старый монстр внутри адаптера

public:
    MonsterAdapter(LegacyMonster* monster);
    ~MonsterAdapter();

    // Реализуем современный интерфейс IEnemy
    void TakeDamage(int damage) override;
    bool IsAlive() override;
    std::string GetName() override;
    int GetHealth() override;
};