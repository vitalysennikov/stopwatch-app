# Удаление сохранённых имён сессий

> **Статус: реализовано, ждёт проверки на устройстве** (сборки только через GitHub Actions).

## Постановка задачи

Имена сохранённых сессий (`session_names`, список в выпадающем меню поля
«Наименование») можно было только переименовывать (`renameSessionName`).
Нужна возможность удалить ненужное имя из этого списка.

Требование по ходу обсуждения расширилось: удаление должно быть возможно
и для имени, с которым связана история сессий — но тогда пользователь должен
получить явное предупреждение с количеством сессий и периодом дат, которые
будут удалены вместе с именем.

## Реализация

### `SessionDao.kt`
Добавлены запросы для оценки последствий удаления и самого каскадного удаления:
```kotlin
@Query("SELECT DISTINCT name FROM sessions WHERE name IS NOT NULL AND name != ''")
fun getDistinctNamesFlow(): Flow<List<String>>

@Query("SELECT COUNT(*) FROM sessions WHERE name = :sessionName")
suspend fun countSessionsByName(sessionName: String): Int

@Query("SELECT MIN(startTime) FROM sessions WHERE name = :sessionName")
suspend fun minStartTimeByName(sessionName: String): Long?

@Query("SELECT MAX(startTime) FROM sessions WHERE name = :sessionName")
suspend fun maxStartTimeByName(sessionName: String): Long?

@Query("DELETE FROM sessions WHERE name = :sessionName")
suspend fun deleteSessionsByName(sessionName: String)
```
Круги (`laps`) удаляются автоматически по `FOREIGN KEY(sessionId) REFERENCES sessions(id) ON DELETE CASCADE`.

### `StopwatchViewModel.kt`
- `namesWithHistory: StateFlow<Set<String>>` — реактивный набор имён, на которые
  ссылается хотя бы одна сессия (для подсветки кнопки удаления в UI).
- `DeleteNameImpact(name, sessionCount, minStartTime, maxStartTime)` + StateFlow
  `deleteNameImpact` — асинхронно посчитанные последствия удаления конкретного имени.
- `prepareDeleteSessionName(name)` — запускает подсчёт (`countSessionsByName` +
  мин/макс `startTime`, только если count > 0).
- `clearDeleteNameImpact()` — сброс при закрытии диалога.
- `confirmDeleteSessionName(entity)` — каскадно удаляет все сессии этого имени
  (`deleteSessionsByName`), затем саму запись `session_names`
  (`sessionNameDao.deleteById`). Если удаляемое имя было текущим — сбрасывает
  `currentName`.

### `StopwatchScreen.kt`
- Кнопка удаления (иконка корзины) теперь показывается для **любого** сохранённого
  имени в выпадающем списке, не только для имён без истории.
- Иконка подсвечивается цветом `MaterialTheme.colorScheme.error`, если у имени
  есть история (`name in namesWithHistory`) — визуальная подсказка ещё до открытия диалога.
- Диалог подтверждения (`deleteTargetName`):
  - пока `prepareDeleteSessionName` не отработал — индикатор загрузки, кнопка
    «Удалить» отключена;
  - если сессий 0 — обычный текст подтверждения;
  - если сессий больше 0 — текст красным цветом: «связано N сессий за период
    dd.MM.yyyy – dd.MM.yyyy, при удалении они тоже будут удалены безвозвратно».

### Строки
Добавлены `delete_saved_name_message` и `delete_saved_name_with_history_message`
в `values/strings.xml`, `values-ru/strings.xml`, `values-zh/strings.xml`.

## Не проверено на устройстве
- [ ] Удаление имени без истории (простое подтверждение).
- [ ] Удаление имени с историей — корректность подсчёта количества и дат,
      реальное каскадное удаление сессий и кругов.
- [ ] Сброс `currentName`, если удалили текущее выбранное имя.
- [ ] Обновление списка `namesFromHistory`/`usedNames` сразу после удаления
      (реактивность через `sessionNameDao.getAllFlow()`).
- [ ] Корректность подсветки кнопки (`namesWithHistory`) при добавлении/удалении
      сессий в реальном времени, пока выпадающий список открыт.
