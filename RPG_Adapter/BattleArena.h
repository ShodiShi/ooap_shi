#pragma once
#include "IWeapon.h"
#include "IEnemy.h"
#include <vector>

// Арена боя - использует ТОЛЬКО интерфейсы IWeapon и IEnemy
// Не знает про адаптеры!
class BattleArena
{
private:
    std::vector<IWeapon*> weapons;  // Список оружия
    IEnemy* currentEnemy;           // Текущий враг

public:
    BattleArena();
    ~BattleArena();

    // Добавить оружие в арсенал
    void AddWeapon(IWeapon* weapon);

    // Установить врага для боя
    void SetEnemy(IEnemy* enemy);

    // Атаковать врага выбранным оружием
    void Attack(int weaponIndex);

    // Получить список оружия
    std::vector<IWeapon*>& GetWeapons();

    // Получить врага
    IEnemy* GetEnemy();
};