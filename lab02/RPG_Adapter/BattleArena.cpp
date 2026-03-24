#include "BattleArena.h"
#include <iostream>

BattleArena::BattleArena()
    : currentEnemy(nullptr)
{
}

BattleArena::~BattleArena()
{
    // Очищаем оружие
    for (auto weapon : weapons)
    {
        delete weapon;
    }
    weapons.clear();

    // Очищаем врага
    if (currentEnemy)
    {
        delete currentEnemy;
        currentEnemy = nullptr;
    }
}

void BattleArena::AddWeapon(IWeapon* weapon)
{
    weapons.push_back(weapon);
}

void BattleArena::SetEnemy(IEnemy* enemy)
{
    currentEnemy = enemy;
}

void BattleArena::Attack(int weaponIndex)
{
    if (weaponIndex < 0 || weaponIndex >= weapons.size())
    {
        std::cout << "Invalid weapon index!\n";
        return;
    }

    if (!currentEnemy)
    {
        std::cout << "No enemy to attack!\n";
        return;
    }

    if (!currentEnemy->IsAlive())
    {
        std::cout << currentEnemy->GetName() << " is already dead!\n";
        return;
    }

    // Используем ИНТЕРФЕЙС! BattleArena не знает про адаптеры!
    IWeapon* weapon = weapons[weaponIndex];

    std::cout << "\n=== ATTACK ===\n";
    std::cout << "Weapon: " << weapon->GetName() << "\n";

    int damage = weapon->Attack();  // Вызов через интерфейс!

    std::cout << "Damage: " << damage << "\n";
    std::cout << "Target: " << currentEnemy->GetName() << "\n";

    currentEnemy->TakeDamage(damage);  // Вызов через интерфейс!

    std::cout << "Enemy HP: " << currentEnemy->GetHealth() << "\n";

    if (!currentEnemy->IsAlive())
    {
        std::cout << currentEnemy->GetName() << " is DEFEATED!\n";
    }

    std::cout << "==============\n\n";
}

std::vector<IWeapon*>& BattleArena::GetWeapons()
{
    return weapons;
}

IEnemy* BattleArena::GetEnemy()
{
    return currentEnemy;
}