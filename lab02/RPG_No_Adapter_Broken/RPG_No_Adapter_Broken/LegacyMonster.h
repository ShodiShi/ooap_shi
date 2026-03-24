#pragma once
#include <string>

class LegacyMonster {
private:
    std::string monsterName;
    int health;
public:
    LegacyMonster(std::string name, int hp) : monsterName(name), health(hp) {}
    void Hurt(int damage) { health -= damage; }
    bool IsDead() { return health <= 0; }
    std::string GetMonsterName() { return monsterName; }
};