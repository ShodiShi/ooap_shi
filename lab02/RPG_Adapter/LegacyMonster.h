#pragma once
#include <string>

// Старый класс монстра (из старой библиотеки)
// У него методы Hurt() вместо TakeDamage() и IsDead() вместо IsAlive()
class LegacyMonster
{
private:
    std::string monsterName;
    int health;

public:
    LegacyMonster(const std::string& name, int hp);

    // СТАРЫЕ методы (не совместимы с IEnemy!)
    void Hurt(int damage);           // вместо TakeDamage()
    bool IsDead();                   // вместо IsAlive() (логика наоборот!)
    std::string GetMonsterName();    // вместо GetName()
    int GetHP();                     // вместо GetHealth()
};