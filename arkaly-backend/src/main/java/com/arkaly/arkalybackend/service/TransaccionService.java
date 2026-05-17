package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.CategoriaTransaccionDTO;
import com.arkaly.arkalybackend.dto.TransaccionDTO;
import com.arkaly.arkalybackend.models.CategoriasTransaccione;
import com.arkaly.arkalybackend.models.Transaccione;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.CategoriasTransaccioneRepository;
import com.arkaly.arkalybackend.repositories.TransaccioneRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TransaccionService {

    private final TransaccioneRepository transaccioneRepository;
    private final CategoriasTransaccioneRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public TransaccionService(TransaccioneRepository transaccioneRepository,
                              CategoriasTransaccioneRepository categoriaRepository,
                              UsuarioRepository usuarioRepository) {
        this.transaccioneRepository = transaccioneRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // TRANSACCIONES

    // Transacciones del usuario en un rango de fechas
    public List<TransaccionDTO> getTransacciones(Integer idUsuario, LocalDate desde, LocalDate hasta) {
        return transaccioneRepository
                .findByIdUsuarioIdAndFechaBetweenOrderByFechaDesc(idUsuario, desde, hasta)
                .stream().map(this::toDTO).toList();
    }

    // Crea una nueva transacción asociada al usuario y categoría
    public TransaccionDTO crearTransaccion(Integer idUsuario, TransaccionDTO dto) {
        Optional<Usuario> resultadoUsuario = usuarioRepository.findById(idUsuario);

        if (resultadoUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Optional<CategoriasTransaccione> resultadoCat = categoriaRepository.findById(dto.getIdCategoria());

        if (resultadoCat.isEmpty()) {
            throw new RuntimeException("Categoría no encontrada");
        }

        Usuario usuario = resultadoUsuario.get();
        CategoriasTransaccione cat = resultadoCat.get();

        Transaccione t = new Transaccione();
        t.setIdUsuario(usuario);
        t.setIdCategoria(cat);
        t.setTipo(dto.getTipo());
        t.setCantidad(dto.getCantidad());
        t.setDescripcion(dto.getDescripcion());
        t.setFecha(dto.getFecha());
        t.setMes(dto.getFecha().getMonthValue());
        t.setAnio(dto.getFecha().getYear());
        return toDTO(transaccioneRepository.save(t));
    }

    // Elimina una transacción por su id
    public void eliminarTransaccion(Integer id) {
        transaccioneRepository.deleteById(id);
    }

    // CATEGORÍAS

    // Todas las categorías del usuario
    public List<CategoriaTransaccionDTO> getCategorias(Integer idUsuario) {
        return categoriaRepository.findByIdUsuarioId(idUsuario)
                .stream().map(this::toCatDTO).toList();
    }

    // Crea una nueva categoría para el usuario
    public CategoriaTransaccionDTO crearCategoria(Integer idUsuario, CategoriaTransaccionDTO dto) {
        Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultado.get();

        CategoriasTransaccione cat = new CategoriasTransaccione();
        cat.setIdUsuario(usuario);
        cat.setNombre(dto.getNombre());
        cat.setColor(dto.getColor());
        cat.setIcono(dto.getIcono());
        return toCatDTO(categoriaRepository.save(cat));
    }

    // Elimina una categoría por su id
    public void eliminarCategoria(Integer id) {
        categoriaRepository.deleteById(id);
    }

    // MAPPERS

    public TransaccionDTO toDTO(Transaccione t) {
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(t.getId());
        dto.setTipo(t.getTipo());
        dto.setCantidad(t.getCantidad());
        dto.setDescripcion(t.getDescripcion());
        dto.setFecha(t.getFecha());
        dto.setMes(t.getMes());
        dto.setAnio(t.getAnio());
        if (t.getIdCategoria() != null) {
            dto.setIdCategoria(t.getIdCategoria().getId());
            dto.setNombreCategoria(t.getIdCategoria().getNombre());
            dto.setColorCategoria(t.getIdCategoria().getColor());
        }
        return dto;
    }

    public CategoriaTransaccionDTO toCatDTO(CategoriasTransaccione c) {
        CategoriaTransaccionDTO dto = new CategoriaTransaccionDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setColor(c.getColor());
        dto.setIcono(c.getIcono());
        return dto;
    }
}
