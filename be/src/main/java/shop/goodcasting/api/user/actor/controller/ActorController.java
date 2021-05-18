package shop.goodcasting.api.user.actor.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shop.goodcasting.api.user.actor.domain.Actor;
import shop.goodcasting.api.user.actor.repository.ActorRepository;
import shop.goodcasting.api.user.actor.service.ActorServiceImpl;

import java.util.List;
import java.util.Optional;

@Log
@RestController
@RequiredArgsConstructor
@RequestMapping("/actors")
public class ActorController {

    private final ActorServiceImpl service;
    private final ActorRepository repo;

    @GetMapping("/list")
    public ResponseEntity<List<Actor>> actorList(){
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/detail")
    public ResponseEntity<Optional<Actor>> detail(@RequestBody Actor actor){
        return ResponseEntity.ok(service.findById(actor.getActorId()));
    }

    @PutMapping("/update")
    public ResponseEntity<Actor> update(@RequestBody Actor actor){
        return ResponseEntity.ok(repo.save(actor));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Long> delete(@RequestBody Actor actor){
        return ResponseEntity.ok(service.delete(actor));
    }
}