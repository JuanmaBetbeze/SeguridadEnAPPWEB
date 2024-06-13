import pandas as pd

hashObjetivo = input('Ingresar hash: ')

for i in range(1, 12):
    file_name = f"./RainbowTables/md5_rainbow_table_part_{i}.csv"
    data = pd.read_csv(file_name, dtype={"password": "string", "hash": "string"}, low_memory=False)
    resultado = data[data['hash'] == hashObjetivo]
    
    if not resultado.empty:
        break

if resultado.empty:
    print('No se encontró la contraseña')
else:
    contrasena = resultado['password'].to_string(index=False)
    print('Contraseña encontrada: ' + contrasena)
