# Slotless
Slotless plugin to disable equipment and inventory

## Inspiration
Inspired from snowflake accounts such as [RichardTape - Settled](https://www.youtube.com/watch?v=2Jk8e59-Jlo) and building on the work from [Adam](https://github.com/Adam-/runelite-plugins/tree/item-filler), I wanted to play a new game mode that is challenging but not completely locked down. I have a background as a developer so I thought I would give the Runelite Plugin space a go. 

## Description
This plugin allows for an account to lock out both the inventory and equipment with bank filler like items. Currently a placeholder item is needed (AL_KHARID_FLYER is the default) and items are needed in equipment slots to be able to replace with bank fillers. It also removes the equip/wear/weild options (most likely have forgotten some) which forces useless items until those slots are unlocked.

This is made for UIM so there is a lot of bank interface weirdness can be expanded on later. 

## Gamemode Inital idea (Slotless)
A UIM with only one slot and no equipment. For every 10 QP a new slot can be unlocked either randomly or from choice. This allows progress on a highly restricted account. 

## Images
### Inventory
Inventory setup is easy just fill inventory with al karid flyers or some item that is unstackable.

Turning the plugin on will result in:
<table>
  <tr>
    <th>Example (plugin off)</th>
    <th>Filler</th>
    <th>Menu actions</th>
    <th>Equipment actions</th>
  </tr>
  <tr>
    <td><img width="278" alt="inventory_0" src="https://github.com/runelite/plugin-hub/assets/29591318/f3c3d453-604a-4fa9-ac88-2f4ef1d491ec"></td>
    <td><img width="269" alt="inventory_1" src="https://github.com/runelite/plugin-hub/assets/29591318/273303f4-b6af-44ea-b7ee-230d3ee31ccc"></td>
    <td><img width="265" alt="inventory_3" src="https://github.com/runelite/plugin-hub/assets/29591318/ff590186-ad14-42e6-841e-a7dbc333428d"></td>
    <td><img width="301" alt="inventory_4" src="https://github.com/runelite/plugin-hub/assets/29591318/93c14998-a880-4762-98e4-565814d068f4"></td>
  </tr>
</table>

### Equipment
Equipment acts very similar just fill slots with items here are the ones I used on a fresh lvl 1 only UIM.
All give 0 positive or negative bonuses only thing that does anything is the forestry outfit so this could be replaced.
<table>
  <tr>
    <th>Example (plugin off)</th>
    <th>Filler</th>
    <th>Menu actions</th>
  </tr>
  <tr>
    <td><img width="275" alt="equipment_0" src="https://github.com/runelite/plugin-hub/assets/29591318/ed66164a-ff55-463d-92ea-1b9ed8d558ec"></td>
    <td><img width="271" alt="equipment_1" src="https://github.com/runelite/plugin-hub/assets/29591318/005272f7-433d-462a-a2c2-ab4c3b7f57d2"></td>
    <td><img width="269" alt="equipment_3" src="https://github.com/runelite/plugin-hub/assets/29591318/cde11281-9373-407e-a435-80df2965872a"></td>
  </tr>
</table>


## Future improvements
 - Quest Point auto unlock (has to be somewhat manual atm by dropping the placeholder or unequiping said slot item)
 - Black list items instead of forcing fillers
 - Handle the Wear/Wield/Equip options better by determining what category that item sits in
 - Will try to maintain this if anyone activly uses it
