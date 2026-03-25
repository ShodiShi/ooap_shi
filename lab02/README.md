# Лабораторная работа №2: Паттерн «Адаптер» (Adapter)

Учебный проект на C++, демонстрирующий применение структурного паттерна проектирования **Adapter** для обеспечения совместимости интерфейсов боевой системы.

---

## 1. Описание проблемы

**Предметная область:** Боевая система RPG-игры (`BattleArena`).  
Система спроектирована для работы с современными абстракциями противников и оружия через интерфейсы `IEnemy` и `IWeapon`.

**Конфликт интерфейсов:**
- Текущая архитектура ожидает методы: `Attack()` и `Use()`
- Сторонние библиотеки (`LegacyMonster`, `OldWeapon`) используют несовместимые сигнатуры: `SpecificLegacyStrike()` и `OldFireMethod()`

**Проблемы без паттерна:**
1. **Нарушение OCP (Open/Closed Principle):** Добавление каждого нового «старого» класса требует переписывания логики арены.
2. **Нарушение инкапсуляции:** Клиентский код вынужден знать детали реализации стороннего кода.
3. **Связность (Coupling):** Высокая зависимость от конкретных реализаций вместо абстракций.

---

## 2. Решение: паттерн Adapter

Паттерн **Adapter** выступает «прослойкой», которая транслирует вызовы из современного интерфейса (`Target`) в методы устаревшего класса (`Adaptee`). В проекте реализован **адаптер объектов** через композицию.

### MonsterAdapter
Адаптирует `LegacyMonster` к интерфейсу `IEnemy`:
```cpp
class MonsterAdapter : public IEnemy {
private:
    LegacyMonster* _monster; // Adaptee
public:
    MonsterAdapter(LegacyMonster* m) : _monster(m) {}

    void Attack() override {
        _monster->SpecificLegacyStrike(); // Attack -> SpecificLegacyStrike
    }

    int GetHealth() override {
        return _monster->CheckVitality();
    }
};
```

### WeaponAdapter
Адаптирует `OldWeapon` к интерфейсу `IWeapon`:
```cpp
class WeaponAdapter : public IWeapon {
private:
    OldWeapon* _weapon; // Adaptee
public:
    WeaponAdapter(OldWeapon* w) : _weapon(w) {}

    void Use() override {
        _weapon->OldFireMethod(); // Use -> OldFireMethod
    }
};
```

---

## 3. Диаграмма классов

<img width="548" height="742" alt="image" src="https://github.com/user-attachments/assets/301c6529-9809-4ce1-87cc-dbc22eca0e48" />





## 4. Вывод

| Аспект | Без паттерна | С паттерном |
| :--- | :--- | :--- |
| **Инкапсуляция** | Нарушена (знание о полях Legacy) | Соблюдена (работа через интерфейс) |
| **Принцип OCP** | Нарушен (нужна правка арены) | Соблюден (добавляем только адаптер) |
| **Связность** | Tight Coupling (зависимость от классов) | Loose Coupling (зависимость от `IEnemy`) |

**Итог:** Паттерн обеспечил прозрачную интеграцию стороннего кода, сохранив гибкость и чистоту основной архитектуры. Разница проявляется на уровне кода: в его читаемости и соблюдении принципов ООП.

---

**Стек:** C++ · SFML · Pattern: Adapter
